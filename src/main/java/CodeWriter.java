import javax.imageio.IIOException;
import java.io.BufferedWriter;
import java.io.IOException;


public class CodeWriter {

    private BufferedWriter output;
    String fileName; //saves the file name for static memory

    private int ifIndex = 1; //index for if conditions in eq, gt,lt commands

    private final int TEMP_INDEX=5;
    private final String ADDR= "addr";
    private final String SP= "SP";


    public CodeWriter(BufferedWriter writer, String fileName){
        this.output=writer;
        this.fileName = fileName.substring(0, fileName.indexOf(".")); //saves the fileName without the extension
    }

    public void writeArithmetic (String command) throws IOException{

        spMinus();
        if(command.equals("add"))
        {
            /*
              @sp
             * A=M
             * D=M
             * sp--
             * A=M
             * D=D+M
             * M=D
             * sp++
             */
            D_sp();
            spMinus();
            output.write("A=M\n");
            output.write("M=D+M\n");
        }

        if(command.equals("sub"))
        {
            D_sp(); // D=RAM[SP]
            spMinus();
            output.write("A=M\n");
            output.write("M=M-D\n");
        }
        if(command.equals("neg"))
        {
            output.write("@"+SP+"\n");
            output.write("A=M\n");
            output.write("M=-M\n");
        }
        if(command.equals("eq")) //using the ifIndex to create diffrent variable for every condition
        {
            /*
              D=RAM[sp]
              sp--
              @Sp
             * A=M
             * D=D-M
             * @true1
             * D:JEQ // d==0
             * @sp
             * A=M
             * M=0
             * @false1
             * 0;jmp
             * (true1)
             * @sp
             * A=M
             * M=-1
             * (false1)
             */
            D_sp(); // D=RAM[sp]
            spMinus();
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("D=D-M\n");
            output.write("@" + "TRUE"+ifIndex+"\n");
            output.write("D;JEQ\n");
            ifLines();
            ifIndex++;
        }
        if(command.equals("gt"))
        {
            D_sp(); // D=RAM[sp]
            spMinus();
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("D=D-M\n");
            output.write("@" + "TRUE"+ifIndex+"\n");
            output.write("D;JLT\n");
            ifLines();
            ifIndex++;
        }
        if(command.equals("lt"))
        {
            D_sp(); // D=RAM[sp]
            spMinus();
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("D=D-M\n");
            output.write("@" + "TRUE"+ifIndex+"\n");
            output.write("D;JGT\n");
            ifLines();
            ifIndex++;
        }
        if(command.equals("and"))
        {
            D_sp();
            spMinus();
            output.write("A=M\n");
            output.write("M=D&M\n");
        }
        if(command.equals("or"))
        {
            D_sp(); // D=RAM[SP]
            spMinus();
            output.write("A=M\n");
            output.write("M=D|M\n");
        }
        if(command.equals("not"))
        {
            output.write("@"+SP+"\n");
            output.write("A=M\n");
            output.write("M=!M\n");
        }
        spPlus(); //in all cases puts the pointer in the end of the stack

    }


    //writes the reparative lines for if statements
    private void ifLines() throws IOException
    {
        output.write("@" +SP+ "\n"+
                "A=M\n"+
                "M=0\n"+
                "@SKIP"+ifIndex+"\n"+
                "0;JMP\n"+
                "(TRUE"+ifIndex+")\n"+
                "@SP\n"+
                "A=M\n"+
                "M=-1\n"+
                "(SKIP"+ifIndex+")\n");
    }


    // writes lines for D=RAM[sp]
    private void D_sp() throws IOException
    {
        output.write("@"+SP+"\n");
        output.write("A=M\n");
        output.write("D=M\n");
    }

    /**
     *
     * @param command
     * @param arg1
     * @param arg2
     * @throws IOException
     */

    public void writePushPop (Parser1.commandType command, String arg1, int arg2 ) throws IOException{
        boolean segmentArg = arg1.equals("local") || arg1.equals("argument") || arg1.equals("this") || arg1.equals("that");
        if(command == Parser1.commandType.C_PUSH) // handling push argument
        {
            if(arg1.equals("constant")) //handling constant
            {
                ram_sp_eq_num(arg2); //RAM[sp] = arg2

            }
            if(segmentArg) //local or argument or this or that
            {
                addr_arg_i(convertSegment(arg1), arg2); //addr = segmentPointer + i
                ptrToPtr(SP, ADDR);//RAM[SP] = RAM[addr]
            }
            if(arg1.equals("static"))
            {
                //pointer = index
                // RAM[SP] = RAM[filename.i]
                output.write("@"+(fileName+"."+arg2)+"\n");
                output.write("D=M\n");
                output.write("@"+SP+"\n");
                output.write("A=M\n");
                output.write("M=D\n");
            }
            if(arg1.equals("temp"))
            {
                // RAM[SP] = RAM[5+i]
                output.write("@"+(TEMP_INDEX+arg2)+"\n");
                output.write("D=M\n");
                output.write("@"+SP+"\n");
                output.write("A=M\n");
                output.write("M=D\n");
            }
            if(arg1.equals("pointer"))
            {
                if(arg2==0) // push THIS
                {
                    //RAM[SP] = RAM[THIS-not as pointer]
                    output.write("@THIS\n");
                    output.write("D=M\n");
                    output.write("@"+SP+"\n");
                    output.write("A=M\n");
                    output.write("M=D\n");
                }
                if(arg2==1)
                {
                    //RAM[SP] = RAM[THAT-not as pointer]
                    output.write("@THAT\n");
                    output.write("D=M\n");
                    output.write("@"+SP+"\n");
                    output.write("A=M\n");
                    output.write("M=D\n");
                }
            }
            spPlus();
        }
        else //pop argument
        {
            if(segmentArg)
            {
                addr_arg_i(convertSegment(arg1), arg2); //addr = segmentPointer + i
                spMinus(); //sp--
                ptrToPtr(ADDR, SP);//RAM[addr] = RAM[sp]
            }

            if(arg1.equals("static"))
            {
                spMinus(); //sp--
                // RAM[filename.i] = RAM[sp]
                output.write("@"+SP+"\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@"+(fileName+"."+arg2)+"\n");
                output.write("M=D\n");
            }
            if(arg1.equals("temp"))
            {
                spMinus();//SP--
                // RAM[5+i] = RAM[sp]
                output.write("@"+SP+"\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@"+(TEMP_INDEX+arg2)+"\n");
                output.write("M=D\n");
            }
            if(arg1.equals("pointer"))
            {
                if(arg2==0) // pop THIS
                {
                    spMinus(); //sp--
                    //RAM[THIS] = RAM[sp]
                    output.write("@"+SP+"\n");
                    output.write("A=M\n");
                    output.write("D=M\n");
                    output.write("@"+"THIS"+"\n");
                    output.write("M=D\n");

                }
                if(arg2==1) // pop THAT
                {
                    spMinus();//sp--
                    //RAM[THAT] = RAM[sp]
                    output.write("@"+SP+"\n");
                    output.write("A=M\n");
                    output.write("D=M\n");
                    output.write("@"+"THAT"+"\n");
                    output.write("M=D\n");

                }
            }
        }


    }

    /**
     * converts segments to their name in memory
     *
     */
    private String convertSegment(String segment)
    {
        switch(segment){
            case "local":
                return "LCL";
            case "argument":
                return "ARG";
            case "this":
                return "THIS";
            case "that":
                return "THAT";
        }
        return null;

    }

    /**
     * writes SP++
     * @throws IOException
     */

    private void spPlus () throws IOException
    {
        output.write("@SP\n");
        output.write("M=M+1\n");
    }


    /**
     * writes SP--
     * @throws IOException
     */
    private void spMinus () throws IOException
    {
        output.write("@SP\n");
        output.write("M=M-1\n");
    }

    /**
     * write addr = arg+i
     * @throws IOException
     */
    private void addr_arg_i (String arg, int i) throws IOException
    {
        /*
          @i
         * D=A
         * @arg
         * D=D+M
         * @addr
         * M=D
         */
        output.write("@"+i+"\n");
        output.write("D=A"+"\n");
        output.write("@"+arg+"\n");
        output.write("D=D+M\n");
        output.write("@addr\n");
        output.write("M=D\n");
    }

    /**
     * writes RAM[target] = RAM[source], when both target and source are "pointers" cells
     * @throws IOException
     */

    private void indexEqPtr (String indexTarget, String ptrSource)  throws IOException
    {
        output.write("@"+ptrSource+"\n");
        output.write("A=M\n");
        output.write("D=M\n");
        output.write("@"+(indexTarget)+"\n");
        output.write("M=D\n");
    }


    //not in use
    private void ptrEqIndex (String ptrTarget, String indexSource)  throws IOException
    {
        output.write("@"+(indexSource)+"\n");
        output.write("D=M\n");
        output.write("@"+ptrTarget+"\n");
        output.write("A=M\n");
        output.write("M=D\n");
    }


    //not in use
    private void ptrToPtr (String target, String source) throws IOException
    {
        /*
          RAM[target] = RAM[source]
          @source
         * A=M
         * D=M
         * @target
         * A=M
         * M=D
         */

        output.write("@"+source+"\n");
        output.write("A=M\n");
        output.write("D=M\n");
        output.write("@"+target+"\n");
        output.write("A=M\n");
        output.write("M=D\n");
    }

    /**
     * RAM[sp] = RAM[num]
     */
    private void ram_sp_eq_num(int num) throws IOException
    {
        //D=num
        output.write("@"+num+"\n");
        output.write("D=A\n");
        //Ram[sp] = D
        output.write("@SP\n");
        output.write("A=M\n");
        output.write(("M=D\n"));

    }

    /**
     * writes the end loop to the file
     */
    public void endLines () throws IOException
    {
        output.write("(END)\n"+
                        "@END\n"+
                        "0;JMP\n");
    }


}
