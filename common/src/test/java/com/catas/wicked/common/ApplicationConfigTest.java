package com.catas.wicked.common;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class ApplicationConfigTest {

    @Test
    @Ignore
    public void testLoadSettings() throws IOException {
        ApplicationConfig appConfig = new ApplicationConfig();
        appConfig.init();
        appConfig.setSettingPath("D:\\PY_Projects\\config\\config.json");
        appConfig.loadSettings();
        assert appConfig.getSettings() != null;
    }

    @Test
    @Ignore
    public void testUpdateSettings() throws IOException {
        ApplicationConfig appConfig = new ApplicationConfig();
        appConfig.init();
        appConfig.setSettingPath("D:\\PY_Projects\\config\\config.json");
        appConfig.loadSettings();

        appConfig.getSettings().setLanguage("English");
        appConfig.getSettings().setCertType(Settings.CertType.CUSTOM);
        appConfig.getSettings().setLocalCertificate(new File("D:\\PY_Projects\\config\\test.cert"));
        appConfig.updateSettings();
    }

    public static void testInput() {
        Scanner scanner = new Scanner(System.in);


        while (scanner.hasNext()) {
            // int n = scanner.nextInt();
            // int x = scanner.nextInt();
            // int y = scanner.nextInt();
            // String next = scanner.next();
            //
            // System.out.printf("n=%s, x=%s, y=%s", n, x, y);

            int row = scanner.nextInt();
            int col = scanner.nextInt();
            int[][] matrix = new int[row][col];
            scanner.nextLine();
            for (int i=0; i < row; i++) {
                for (int j=0; j < col; j++) {
                    matrix[i][j] = scanner.nextInt();
                }
            }

            outputMatrix(matrix);
        }
    }

    private static void outputMatrix(int[][] matrix) {
        if (matrix == null) {
            return;
        }
        for (int i=0; i < matrix.length; i++) {
            for (int j=0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print("\n");
        }
    }


    public static void main(String[] args) {
        testInput();

        int[][] p = new int[5][5];

        Arrays.sort(p, (o1, o2) -> o1[0] > o2[0] ? 1 : Integer.compare(o1[1], o2[1]));
    }


}
