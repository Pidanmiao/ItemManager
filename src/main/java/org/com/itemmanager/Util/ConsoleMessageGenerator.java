package org.com.itemmanager.Util;

public class ConsoleMessageGenerator {
    public enum ErrorType
    {
        NotFound,
        syntaxError
    }
    public static final String[] ConsoleMethodList = new String[]
            {
              "help", "list"
            };
    private static final String[] HelpList = new String[]
            {
                    "help: ",
                    "list: "
            };

    public static String GenerateErrorMessage(ErrorType errorType, String ErrorSource)
    {
        switch (errorType)
        {
            case NotFound:
                return "No such method "+ ErrorSource +".Use 'help' for more information.";
            case syntaxError:
                return "Syntax error.Use 'help "+ErrorSource+"' for help.";
            default:
                return "Unknown Error";
        }
    }

    public static String GenerateHelpMessage(int helpTarget)
    {
        if(helpTarget <0)
        {
            String res = "";
            for (String s: HelpList) {
                res += s+"\n";
            }
            return res;
        }
        else if(helpTarget < HelpList.length)
        {
            return  HelpList[helpTarget];
        }
        else
        {
            return  "Unknown Error";
        }
    }
}
