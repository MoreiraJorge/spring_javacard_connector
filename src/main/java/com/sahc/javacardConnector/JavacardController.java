package com.sahc.javacardConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JavacardController {

    private JavacardService cardService;

    @Autowired
    public JavacardController(JavacardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/")
    public String helloWorld() throws Exception {
        try {
            cardService.setApdu();
            byte[] apduCommand = new byte[4];
            apduCommand[0] = (byte) 128;
            apduCommand[1] = (byte) 16;
            apduCommand[2] = (byte) 0;
            apduCommand[3] = (byte) 0;

            System.out.println("----------APDU-----------");
            cardService.setTheAPDUCommands(apduCommand);

            cardService.setExpctdByteLength((byte) 8);
            cardService.exchangeTheAPDUWithSimulator();
            cardService.decodeStatus();
            System.out.println("-------------------------");

            return "APDU Route";
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @GetMapping("/conect")
    public String connect() throws Exception {
        try {
            cardService.establishConnectionToSimulator();
            return "Connection established!";
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
