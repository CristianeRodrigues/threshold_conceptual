/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.gems.lcoupling.threshold_conceptual;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Cristiane
 */
public class readConceptualCoupling {

    public static void main(String[] args) throws IOException {

        /*String input = "C:\\Users\\Carlos\\projects\\shapes";
        double threshold = 0.01;*/

        final Options options = new Options();

        String input = "";
        Double threshold = 0.0;

        try {
            options.addOption("i", true, "input directory");
            options.addOption("t", true, "threshold from 0.0 to 1.0");

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("conceptual-coupling", options, true);

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("i")) {
                input = cmd.getOptionValue("i");
            }
            if (cmd.hasOption("t")) {
                threshold = Double.parseDouble(cmd.getOptionValue("t"));
            }
        } catch (ParseException ex) {
            Logger.getLogger(readConceptualCoupling.class.getName()).log(Level.SEVERE, null, ex);

        }
        System.out.println("Creating file with threshold " + threshold );
        createFinalResultThreshold(input, threshold);
        
        System.out.println("Calculating the metrics with threshold " + threshold );
        calculateMetrics(input, threshold);
        
        System.out.println("Finished!");
    }

    public static void createFinalResultThreshold(String filePath, double option_threshold) throws IOException {

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().startsWith("FinalResult");
            }
        };

        String filePath_output = filePath + File.separator + "Output" + File.separator;
        String filePathName = filePath_output + "Threshold" + option_threshold + ".txt";
        try (FileWriter arquivo = new FileWriter(new File(filePathName))) {
            File directory = new File(filePath_output);
            File files[] = directory.listFiles(filter);
            for (File file : files) {
                FileInputStream stream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(reader);
                Double threshold;
                Double total_intensity = 0.0;
                String SHAMerge = "";
                String similarity;
                String leftMethod;
                String rightMethod;
                int coupling = 0;
                String line = br.readLine();
                while (line != null) {
                    SHAMerge = line.substring(0, 40);
                    leftMethod = line.substring(41, line.indexOf(",R"));
                    rightMethod = line.substring(line.indexOf(",R") + 1, line.lastIndexOf(','));
                    similarity = line.substring(line.lastIndexOf(',') + 1, line.length());
                    line = br.readLine();
                    threshold = Double.parseDouble(similarity);

                    if ((threshold >= option_threshold) && (threshold < option_threshold + 0.1)) {// It calculates the intensity according to the threshold

                        arquivo.write(SHAMerge + "," + leftMethod + "," + rightMethod + "," + threshold + "\n");
                    }
                }
            }
            arquivo.close();
        }

    }

    public static void calculateMetrics(String filePath, double option_threshold) throws IOException {

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().startsWith("Threshold" + option_threshold);
            }
        };

        String filePath_output = filePath + File.separator + "Output" + File.separator;
        String filePathName = filePath_output + "ResultThreshold" + option_threshold + ".txt";
        try (FileWriter arquivo = new FileWriter(new File(filePathName))) {
            File directory = new File(filePath_output);
            File files[] = directory.listFiles(filter);
            for (File file : files) {
                FileInputStream stream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(reader);
                Double threshold;
                Double total_intensity = 0.0;
                String SHAMerge = "";
                String SHAMergeAnt = "";
                String similarity;
                //String leftMethod;
                //String rightMethod;
                String filePath_input = "";
                int coupling = 0;
                double normalized_coupling = 0;
                String line = br.readLine();
                SHAMergeAnt = line.substring(0, 40);
                while (line != null) {
                    SHAMerge = line.substring(0, 40);

                    if (SHAMerge.equals(SHAMergeAnt)) {
                        similarity = line.substring(line.lastIndexOf(',') + 1, line.length());
                        threshold = Double.parseDouble(similarity);
                        total_intensity = total_intensity + threshold; // It calculates the intensity according to the threshold
                        line = br.readLine();
                        if (line == null) {
                            filePath_input = filePath + File.separator + "Output" + File.separator;
                            String filePathName_input = filePath_input + "MergeConceptualCoupling" + SHAMergeAnt + ".txt";
                            File arquivoLeitura = new File(filePathName_input);
                            long tamanhoArquivo = arquivoLeitura.length();
                            FileInputStream fs = new FileInputStream(filePathName_input);
                            DataInputStream in = new DataInputStream(fs);

                            LineNumberReader lineRead = new LineNumberReader(new InputStreamReader(in));
                            lineRead.skip(tamanhoArquivo);

                            int pairs_cartesian_product = lineRead.getLineNumber();

                            if (pairs_cartesian_product > 0) {
                                normalized_coupling = total_intensity / pairs_cartesian_product;
                                arquivo.write(SHAMergeAnt + "," + pairs_cartesian_product + "," + total_intensity + "," + normalized_coupling + "\n");
                                SHAMergeAnt = SHAMerge;
                                total_intensity = 0.0;
                                pairs_cartesian_product = 0;
                            }

                        }
                    } else if (!(SHAMerge.equals(SHAMergeAnt))) {
                        //chunks is the set of modified methods, that is, number of text files

                        filePath_input = filePath + File.separator + "Output" + File.separator;
                        String filePathName_input = filePath_input + "MergeConceptualCoupling" + SHAMergeAnt + ".txt";
                        File arquivoLeitura = new File(filePathName_input);
                        long tamanhoArquivo = arquivoLeitura.length();
                        FileInputStream fs = new FileInputStream(filePathName_input);
                        DataInputStream in = new DataInputStream(fs);

                        LineNumberReader lineRead = new LineNumberReader(new InputStreamReader(in));
                        lineRead.skip(tamanhoArquivo);

                        int pairs_cartesian_product = lineRead.getLineNumber();

                        if (pairs_cartesian_product > 0) {
                            normalized_coupling = total_intensity / pairs_cartesian_product;
                            arquivo.write(SHAMergeAnt + "," + pairs_cartesian_product + "," + total_intensity + "," + normalized_coupling + "\n");
                            SHAMergeAnt = SHAMerge;
                            total_intensity = 0.0;
                            pairs_cartesian_product = 0;
                        }
                    }
                }
                arquivo.close();
            }
        }
    }
}
