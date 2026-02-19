package com.eliza;

import java.util.Random;
import java.util.Scanner;

/**
 * Console interface for the ELIZA chatbot.
 * Type "quit" or "bye" to exit.
 */
public class Main {

    private static final String INTRO = """
            ╔══════════════════════════════════════════════════════════════╗
            ║                        E L I Z A                             ║
            ║          A Rogerian Psychotherapist Simulation               ║
            ║       Based on Joseph Weizenbaum's 1966 program              ║
            ║                                                              ║
            ║  Type anything to talk to ELIZA. Type "quit" to exit.        ║
            ╚══════════════════════════════════════════════════════════════╝
            """;

    private static final String[] GREETINGS = {
            "Hello. I'm ELIZA. How are you feeling today?",
            "Hello. I'm ELIZA. What is troubling you?",
            "Hello, I am ELIZA. What is your name?",
            "Hello, I am ELIZA. What is your problem?",
            "Welcome. I am ELIZA. Please tell me your name.",
            "Welcome. I am ELIZA. Tell me what is on your mind.",
    };

    public static void main(String[] args) {
        Eliza eliza = new Eliza();
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.println(INTRO);
        System.out.println("ELIZA: " + GREETINGS[random.nextInt(GREETINGS.length)] + "\n");

        while (true) {
            System.out.print("You:   ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("bye")
                    || input.equalsIgnoreCase("exit")) {
                System.out.println("\nELIZA: Goodbye. Thank you for talking with me.");
                break;
            }

            String response = eliza.respond(input);
            System.out.println("ELIZA: " + response + "\n");

            if (eliza.hasParityError()) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                System.out.println("    *** SYSTEM HALTED ***");
                System.out.println("    *** MEMORY DUMP: 0x0000 - 0xFFFF ***");
                System.out.println("    *** FATAL EXCEPTION IN MODULE ELIZA.EXE ***");
                System.out.println("    *** REBOOT REQUIRED ***\n");
                break;
            }
        }

        scanner.close();
    }
}
