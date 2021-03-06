package websearch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class ComplementErrorPage
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        int errorCount = 0;
        for (int i = 0; i < 1000; i++)
        {
            System.out.println("Start file " + i);
            String fileName = "query.data/" + i;
            String toFileName = fileName + ".added";
            String toFileNext = "query.data/" + (i + 1) + ".added";
            File f1 = new File(toFileName);
            File f2 = new File(toFileNext);
            if (f1.exists() && f2.exists())
                continue;
            String format = "%s\n%s\n%s\n%s\n%s\n";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(toFileName)));
            String startLine = null;
            while((startLine = reader.readLine()) != null)
            {
                String title = reader.readLine();
                String snippet = reader.readLine();
                String url = reader.readLine();
                String body = reader.readLine().replaceAll("\\&[a-zA-z];", "").replaceAll("\\s+", " ").trim();
                if (body.length() < 1)
                {
                    System.out.print(startLine);
                    System.out.print(" Empty ");
                    try
                    {
                        body = HtmlParserDriver.getBodyTextGoogleCached(url).replaceAll("&[a-zA-z];", "").replaceAll("\\s+", " ").trim();
                        if (url.length() > 1) System.out.print("Success");
                        System.out.println();
                        Thread.sleep(2000);
                    } catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        errorCount++;
                        e.printStackTrace();
                        Thread.sleep(60000);
                    }
                }
                writer.write(String.format(format, startLine, title, snippet, url, body));
            }
            writer.close();
        }
    }
}
