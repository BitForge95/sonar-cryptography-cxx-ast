package org.pqca.cbomkit;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;

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

        public String toJson() {
            return String.format(
                "    {\n      \"ruleId\": \"%s\",\n      \"apiCall\": \"%s\",\n      \"filePath\": \"%s\",\n      \"lineNumber\": %d\n    }",
                ruleId, apiCall, filePath, lineNumber
            );
        }
    }

    public static void main(String[] args) {
        String[] targetFiles = {
            "src/test/resources/openssl_test.c",
            "src/test/resources/botan_test.cpp"
        };
        
        final List<CbomFinding> findingsList = new ArrayList<>();

        for (String targetFile : targetFiles) {
            if (!new File(targetFile).exists()) {
                System.err.println("Error: Target file not found -> " + targetFile);
                continue; // Skip to the next file if one is missing
            }
            
            try {
                // 1. Initialize CDT Headless Parser components
                FileContent fileContent = FileContent.createForExternalFileLocation(targetFile);
                Map<String, String> macroDefinitions = new HashMap<>();
                macroDefinitions.put("__cplusplus", "201703L");
                
                ScannerInfo scannerInfo = new ScannerInfo(macroDefinitions, new String[0]);
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

               ASTVisitor visitor = new ASTVisitor() {
                    {
                        // Explicitly tell the walker we care about expression trees and declarations
                        shouldVisitExpressions = true; 
                        shouldVisitDeclarations = true;
                    }

                    @Override
                    public int visit(IASTExpression expression) {
                        // Check if the current node is a function call
                        if (expression instanceof IASTFunctionCallExpression) {
                            IASTFunctionCallExpression callExpr = (IASTFunctionCallExpression) expression;
                            IASTExpression nameExpr = callExpr.getFunctionNameExpression();
                            
                            String signature = nameExpr.getRawSignature();
                            
                            // Match the targeted cryptographic API call
                            if ("EVP_PKEY_CTX_new_from_name".equals(signature)) {
                                String rawArgs = callExpr.getRawSignature();
                                String ruleId = rawArgs.contains("\"RSA\"") ? "CPP_OPENSSL_LEGACY_RSA" : "CPP_OPENSSL_PQC_MLKEM";
                                
                                findingsList.add(new CbomFinding(
                                    ruleId,
                                    signature,
                                    targetFile,
                                    callExpr.getFileLocation().getStartingLineNumber()
                                ));
                            }
                        }
                        return PROCESS_CONTINUE;
                    }

                    @Override
                    public int visit(IASTDeclaration declaration) {
                        String rawDecl = declaration.getRawSignature();
                        
                        // Match the targeted cryptographic API call for Botan C++ objects
                        if (rawDecl != null && rawDecl.contains("Botan::ML_KEM_PrivateKey")) {
                            findingsList.add(new CbomFinding(
                                "CPP_BOTAN_PQC_MLKEM",
                                "Botan::ML_KEM_PrivateKey",
                                targetFile,
                                declaration.getFileLocation().getStartingLineNumber()
                            ));
                        }
                        return PROCESS_CONTINUE;
                    }
                };

                translationUnit.accept(visitor);
                
            } catch (CoreException e) {
                System.err.println("CDT Parsing Error: Failed to generate AST Translation Unit for " + targetFile);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected execution error occurred.");
                e.printStackTrace();
            }
        }

        // Build CycloneDX CBOM Wrappeer
        System.out.println("{");
        System.out.println("  \"bomFormat\": \"CycloneDX\",");
        System.out.println("  \"specVersion\": \"1.6\",");
        System.out.println("  \"components\": [");
        
        for (int i = 0; i < findingsList.size(); i++) {
            System.out.print(findingsList.get(i).toJson());
            if (i < findingsList.size() - 1) System.out.println(",");
            else System.out.println();
        }
        
        System.out.println("  ]");
        System.out.println("}");
    }
}