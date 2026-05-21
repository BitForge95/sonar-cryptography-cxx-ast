package org.pqca.cbomkit;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This engine utilizes Eclipse CDT to perform headless AST parsing
 * of C/C++ source files to detect cryptographic primitives.
 */
public class CppCryptoScanner {

    static class CbomFinding {
        String ruleId;
        String apiCall;
        String filePath;
        int lineNumber;

        CbomFinding(String ruleId, String apiCall, String filePath, int lineNumber) {
            this.ruleId = ruleId;
            this.apiCall = apiCall;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing C++ AST Scanner...");
        String targetFile = "test.cpp";
        
        if (!new File(targetFile).exists()) {
            System.err.println("Error: Target file not found -> " + targetFile);
            return;
        }
        
        // 1. Initialize CDT Headless Parser components
        FileContent fileContent = FileContent.createForExternalFileLocation(targetFile);
        
        Map<String, String> macroDefinitions = new HashMap<>();
        macroDefinitions.put("__cplusplus", "201703L");

        String[] includeSearchPaths = new String[0]; 
        
        ScannerInfo scannerInfo = new ScannerInfo(macroDefinitions, includeSearchPaths);
        IncludeFileContentProvider provider = IncludeFileContentProvider.getEmptyFilesProvider();

        // GPPLanguage specifies that we are parsing C++
        IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(
                fileContent, 
                scannerInfo, 
                provider, 
                null, 
                0, 
                new DefaultLogService()
        );

        final List<CbomFinding> findingsList = new ArrayList<>();

       ASTVisitor visitor = new ASTVisitor() {
            {
                // Explicitly tell the walker we only care about expression trees
                shouldVisitExpressions = true; 
            }

            @Override
            public int visit(IASTExpression expression) {
                // Check if the current node is a function call
                if (expression instanceof IASTFunctionCallExpression) {
                    IASTFunctionCallExpression callExpr = (IASTFunctionCallExpression) expression;
                    IASTExpression nameExpr = callExpr.getFunctionNameExpression();
                    
                    String signature = nameExpr.getRawSignature();
                    
                    // Match the targeted cryptographic API call
                    if ("EVP_EncryptInit_ex".equals(signature)) {
                        findingsList.add(new CbomFinding(
                            "CPP_OPENSSL_EVP_ENCRYPT",
                            signature,
                            targetFile,
                            callExpr.getFileLocation().getStartingLineNumber()
                        ));
                    }
                }
                return PROCESS_CONTINUE;
            }
        };

        translationUnit.accept(visitor);
    }
}