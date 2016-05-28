package ru.hh.test.crawler.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class BoyerMooreSearchTask implements Runnable {
    final BoyerMooreSearchContext context;
    final File file;
    final Pattern pattern ;
    private final Logger logger = LoggerFactory.getLogger(BoyerMooreSearchTask.class);

    public BoyerMooreSearchTask(File file, String search,BoyerMooreSearchContext context) {
        this.file = file;
        this.pattern =  Pattern.compile(search,  Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        this.context = context;
    }

    @Override
    public void run()  {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(pattern.matcher(line).find()){
                    context.addResult(file.getPath());
                    return;
                }
            }
        }
        catch (IOException e){
            logger.warn("Cant read content of file {}",file.getPath(),e);

        }
        finally {
            context.latch.countDown();
        }
    }
}
