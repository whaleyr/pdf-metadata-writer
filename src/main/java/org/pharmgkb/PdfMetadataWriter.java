package org.pharmgkb;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class PdfMetadataWriter {
  private static final String CPIC_AUTHOR = "CPIC";
  final Path directory;
  String author = CPIC_AUTHOR;

  public static void main(String[] args) {

    try {
      Options options = new Options();
      options.addOption("d", true,"directory with pdf files to be writen to");
      options.addOption("a", true,"text for author field, default is CPIC");
      CommandLineParser clParser = new DefaultParser();
      CommandLine cli = clParser.parse(options, args);

      PdfMetadataWriter review = new PdfMetadataWriter(cli.getOptionValue("d"), cli.getOptionValue("a"));
      review.execute();
    } catch (Exception e) {
      System.err.println("Error reading PDF for modification");
      e.printStackTrace();
    }
  }

  PdfMetadataWriter(String dir, String author) {
    this.directory = Paths.get(dir);
    if (author != null) {
      this.author = author;
    }
  }

  void execute() throws IOException {
    Collection<File> files = FileUtils.listFiles(this.directory.toFile(), new String[]{"pdf"}, true);

    SortedSet<File> sortedFiles = new TreeSet<>(files);
    for (File file : sortedFiles) {
      String newTitle = file.getName().replaceAll("_", " ").replaceFirst("\\.pdf", "");

      try (PDDocument pdf = PDDocument.load(file)) {
        PDDocumentInformation info = pdf.getDocumentInformation();
        if (!Objects.equals(info.getTitle(), newTitle) || !Objects.equals(info.getAuthor(), CPIC_AUTHOR)) {
          info.setTitle(newTitle);
          info.setAuthor(CPIC_AUTHOR);
          info.setCreator(null);
          info.setKeywords(null);
          pdf.save(file);

          System.out.println("Updated " + file);
        }
      }
    }
  }
}
