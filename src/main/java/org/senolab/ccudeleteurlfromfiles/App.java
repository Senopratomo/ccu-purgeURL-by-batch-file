package org.senolab.ccudeleteurlfromfiles;

import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.google.api.client.http.HttpResponseException;
import org.senolab.ccudeleteurlfromfiles.model.Delete;
import org.senolab.ccudeleteurlfromfiles.model.Invalidate;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class App {

    public static void main(String[] args) {
        try {
            if(args.length == 5) {
                File[] files = new File(args[3]).listFiles();
                switch(args[1]) {
                    case "delete":
                        if (files != null) {
                            for(File urlsFile : files) {
                                Delete delete = new Delete(args[0], args[2], urlsFile.getAbsolutePath());
                                delete.execute();
                                Thread.sleep(Integer.parseInt(args[4]) * 1000);
                            }
                        } else {
                            System.out.println("The directory path specified either contain no files or invalid");
                            System.exit(0);
                        }
                        break;
                    case "invalidate":
                        if (files != null) {
                            for(File urlsFile : files) {
                                System.out.println("Executing CCU API call for file "+urlsFile.getAbsolutePath());
                                Invalidate invalidate = new Invalidate(args[0], args[2], urlsFile.getAbsolutePath());
                                invalidate.execute();
                                Thread.sleep(Integer.parseInt(args[4]) * 1000);
                            }
                        } else {
                            System.out.println("The directory path specified either contain no files or invalid");
                            System.exit(0);
                        }
                        break;
                    default:
                        System.out.println("Invalid options in args[1]. Please specify the correct option");
                }
            } else {
                printInstructions();
            }
        } catch (ParseException e) {
            System.out.println("Something wrong when parsing JSON body to the request. Details: \n");
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            System.out.println("Something wrong during parsing file containing JSON object. Details: \n");
            e.printStackTrace();
        } catch (HttpResponseException e) {
            System.out.println("Response code: "+e.getStatusCode());
            System.out.println("Response headers: \n"+e.getHeaders());
            System.out.println("Response body: \n"+e.getContent());
        } catch (IOException e) {
            System.out.println("Something wrong during I/O process. Details:");
            e.printStackTrace();
        } catch (RequestSigningException e) {
            System.out.println("Something wrong during authentication process. Details:");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Thread is prematurely interrupted. Details:");
            e.printStackTrace();
        }


    }

    private static void printInstructions() {
        System.out.println("This CLI takes 5 arguments separated by single space. These arguments are: \n" +
                "args[0] is location of .edgerc file. " +
                "This file contain Akamai API client credentials (client token, \n" +
                "access token, secret, host, and max body size) which necessary for EdgeGrid lib \n" +
                "sample: \n" +
                "host = https://akab-xxxxx.luna.akamaiapis.net \n" +
                "client_token = akab-xxxxx \n" +
                "client_secret = xxxxx \n" +
                "access_token = xxxx \n" +
                "\n" +
                "args[1] is type of purge - options are: 'delete' or 'invalidate'\n" +
                "args[2] is target network - options are: 'staging' or 'production'\n" +
                "args[3] is full path to the directory which has file(s) containing either list of URLs or raw CCU JSON to be executed synchronously\n" +
                "args[4] is number of seconds the CLI will pause between each CCU API call execution\n" +
                "\n" +
                "Example:\n" +
                "1) Delete all URLs listed inside all files within /home/user/fileToPurge directory in Staging network\n" +
                "java -jar ccu.jar /home/user/token.txt delete staging /home/user/fileToPurge 5\n" +
                "2) Invalidate all URLs listed inside all files within /home/user/fileToPurge directory in production network\n" +
                "java -jar ccu.jar /home/user/token.txt invalidate production /home/user/fileToPurge 10\n" );
    }

}
