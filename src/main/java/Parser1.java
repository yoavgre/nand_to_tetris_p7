import java.util.Scanner;

public class Parser1 {

    Scanner in;
    String currInst;

    public Parser1(Scanner in)
    {
        this.in = in;
        currInst ="";
    }

    /**
     * @return whether there are more Lines to read
     */
    public boolean hasMoreLines(){
        return in.hasNextLine();
    }

    /**
     * reads the next line, skips empty lines, comment lines
     */
    public void advance()  {
        this.currInst = in.nextLine();
        while(currInst.indexOf('/')!=-1||currInst.trim().isEmpty()) //skips line with a comment or empty line
            if(in.hasNextLine())
                currInst=in.nextLine();
    }

    public enum commandType {C_ARITHMETIC, C_PUSH, C_POP}

    /**
     * returns the command type
     */
    public commandType commType(){
        if(currInst.indexOf("push")!=-1)
            return commandType.C_PUSH;
        if(currInst.indexOf("pop")!=-1)
            return commandType.C_POP;
        else
            return commandType.C_ARITHMETIC;
    }

    /**
     * returns first part of the command
     */

    public String arg1()
    {
        String [] arg = this.currInst.split(" ");
        if(this.commType()==commandType.C_ARITHMETIC)
            return arg[0].trim(); //maybe without trim
        else
            return arg[1].trim(); //maybe without trim
    }


    /**
     * returns second part of the command (only in pop or push)
     */
    public int arg2()
    {
        String [] arg = this.currInst.split(" ");
        return Integer.parseInt(arg[2]);
    }

}
