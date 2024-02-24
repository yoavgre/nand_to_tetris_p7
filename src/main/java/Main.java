

import javax.swing.text.html.parser.Parser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException{
     //   if (args.length == 0) {
       //     System.err.println("No source file");
         //   System.exit(1);
        //}
        File sourceFile = new File("C:\\Users\\יואב\\Desktop\\yearB\\טטריס\\projects\\07\\MemoryAccess\\StaticTest\\StaticTest.vm");
        if (sourceFile.isDirectory()) //source is a directory go assemble all files in directory
        {
            File[] files = sourceFile.listFiles();
            for (File file : files)
            {
                if(file.getName().indexOf(".vm")!=-1)//vm file
                    translateFile(file);
            }

        } else
            translateFile(sourceFile);
    }

    public static void translateFile(File sourceFile) throws IOException {
        if (!sourceFile.exists()) {
            System.err.println("file could not be found.");
            System.exit(2);
        }

        //handling the paths and name of the new .asm file
        String fileName = sourceFile.getName();
        String fileNameNoAsm = fileName.substring(0, fileName.indexOf("vm"));
        String filePath = sourceFile.getAbsolutePath();
        String outDirPath = filePath.substring(0, filePath.indexOf(fileName));
        String outputPath = outDirPath + fileNameNoAsm + "asm";
        File output = new File(outputPath);
        output.createNewFile();

        Scanner sourceScanner = new Scanner(sourceFile);
        BufferedWriter outPutWriter = new BufferedWriter(new FileWriter(output));
        CodeWriter codeWriter = new CodeWriter(outPutWriter, output.getName());
        Parser1 parser = new Parser1 (sourceScanner);
        while(parser.hasMoreLines())
        {
            parser.advance();
            outPutWriter.write("//"+parser.currInst+"\n"); //writes comment of the intended command to the file
            if(parser.commType()== Parser1.commandType.C_ARITHMETIC)
            {
                String arg1 = parser.arg1();
                codeWriter.writeArithmetic(arg1);
            }
            else {
                String arg1 = parser.arg1();
                int arg2 = parser.arg2();
                codeWriter.writePushPop(parser.commType(), arg1, arg2);
            }
        }

        codeWriter.endLines();//write the end loop

        sourceScanner.close();
        outPutWriter.close();





    }
}

