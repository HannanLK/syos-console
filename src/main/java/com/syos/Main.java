package com.syos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        DisplayWelcomeBanner();
        DisplayMainMenu();
    }

    private static void DisplayWelcomeBanner(){
        try {
            InputStream inputStream = Main.class.getResourceAsStream("/static/banner.txt");
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            } else {
                System.out.println("Banner file not found!");
            }
        } catch (IOException e) {
            System.out.println("Error reading banner file: " + e.getMessage());
        }
    }

    private static void DisplayMainMenu(){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Main Menu:");
        System.out.println("1. Browse Products");
        System.out.println("2. Login");
        System.out.println("3. Register");
        System.out.println("4. Exit");
        System.out.print("Enter Your Choice : ");

        int choice = scanner.nextInt();
    }

}