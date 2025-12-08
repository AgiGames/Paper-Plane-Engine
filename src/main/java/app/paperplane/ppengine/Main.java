package app.paperplane.ppengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static PPEngine printEngine = new PPEngine();

    public static void main(String[] args){
        SpringApplication.run(Main.class);
    }

}