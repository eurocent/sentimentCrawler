/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurosentiment.sentiment.crawler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.writer.JSONWriter;
import org.apache.any23.writer.NQuadsWriter;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.RDFXMLWriter;
import org.apache.any23.writer.TriXWriter;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.any23.writer.TurtleWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

/**
 * <p>
 * Runner is the primary class from which we crawl HTTP or file 
 * protocols. Once we successfully locate and fetch am (HTML) document
 * we extract subject, predicate, object relationships and pipe these
 * out to an output directory.</p>
 * <p>
 * The tool is extremely lightweight but there are some nice features
 * <ol>
 *  <li>The output path is configurable (can only store locally on file system)</li>
 *  <li>The output format is configurable and can be one of <b>turtle</b>, <b>ntriples</b>,
 *  <b>rdfxml</b>, <b>nquads</b>, <b>trix</b> or <b>json</b></li>
 * </ol>
 * </p>
 *
 */
public class Runner {

  private static String uri;

  private static String outputDir;

  private static String outputFormat;

  /**
   * <p>The main method used to run the Runner program.
   * We are looking for the following arguments:</p>
   * <ol>
   *  <li>A fully qualified URL to be used as input from which we extract sentiment <b>(mandatory)</b></li>
   *  <li>A writable, local output directory to which we can write sentiment output <b>(mandatory)</b>.
   *  If no output directory is given we just use the directory from which this program was executed.</li>
   *  <li>The output format we wish the sentiment triples to be serialized as <b>(optional)</b>. If
   *  no serialization is provided we simply use turtle. The following arguments are accepted,
   *  <b>turtle</b>, <b>ntriples</b>, <b>rdfxml</b>, <b>nquads</b>, <b>trix</b> or <b>json</b></li>
   * </ol>
   * 
   * @param args
   */
  @SuppressWarnings("static-access")
  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(new Option( "help", "print this message" ));
    options.addOption(OptionBuilder.withArgName( "url" )
        .hasArg()
        .withDescription( "run sentiment extraction on this URL" )
        .create( "url" ));
    options.addOption(OptionBuilder.withArgName( "output_dir" )
        .hasArg()
        .withDescription( "output directory for extracted s, p, o sentiments" )
        .create( "outputDir" ));
    options.addOption(OptionBuilder.withArgName( "output_serialization" )
        .hasArg()
        .withDescription( "output serialization for extracted s, p, o sentiments "
            + "(one of 'turtle', 'ntriples', 'rdfxml', 'nquads', 'trix' or 'json')" )
            .create( "outputFormat" ));

    GnuParser parser = new GnuParser();
    CommandLine cmdLine = null;

    try {
      cmdLine = parser.parse( options, args );
    }
    catch( ParseException exp ) {
      System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
    }

    if( cmdLine.hasOption( "help" ) ) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( Runner.class.getSimpleName(), options );
      System.exit(0);
    }
    if (cmdLine.hasOption("url")) {
      setUri(cmdLine.getOptionValue( "url" ));
    }
    if (cmdLine.hasOption("outputDir")) {
      setOutputDir(cmdLine.getOptionValue( "outputDir" ));
    }
    if (cmdLine.hasOption("outputFormat")) {
      setOutputFormat(cmdLine.getOptionValue("outputFormat"));
    }

    try {
      run(uri, outputDir, outputFormat);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ExtractionException e) {
      e.printStackTrace();
    }
  }

  private static void run(String uri, String outputDir, String outputFormat) throws IOException, URISyntaxException, ExtractionException {
    Any23 runner = new Any23();
    runner.setHTTPUserAgent("Eurosentiment Crawler");
    HTTPClient httpClient = runner.getHTTPClient();
    DocumentSource source = new HTTPDocumentSource(
        httpClient,
        uri
        );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TripleHandler handler = null;
    if (outputFormat != null) {
      switch (outputFormat) {
      case "turtle":
        handler = new TurtleWriter(out);
        break;
      case "ntriples":
        handler = new NTriplesWriter(out);
        break;
      case "rdfxml":
        handler = new RDFXMLWriter(out);
        break;
      case "nquads":
        handler = new NQuadsWriter(out);
        break;
      case "trix":
        handler = new TriXWriter(out);
        break;
      case "json":
        handler = new JSONWriter(out);
        break;
      default:
        System.out.println("No output writer found for type: " + outputFormat);
        System.out.println("Defaulting to Turtle output serialization");
        handler = new TurtleWriter(out);
        break;
      }
      System.out.println("Selected " + handler.getClass().getSimpleName() + " as output writer.");
    }
    try {
      runner.extract(source, handler);
    } finally {
      try {
        handler.close();
      } catch (TripleHandlerException e) {
        e.printStackTrace();
      }
    }
    if (outputDir != null) {
      FileUtils.writeStringToFile(new File(outputDir + "/sentiment.txt"), out.toString("UTF-8"));
      System.out.println("Successfully wrote file to: " + outputDir + "/sentiment.txt");
    } else {
      FileUtils.writeStringToFile(new File("sentiment.txt"), out.toString("UTF-8"));
      System.out.println("Successfully wrote file to sentiment.txt");
    }
  }

  /**
   * @return the uri
   */
  public static String getUri() {
    return uri;
  }

  /**
   * @param uri the uri to set
   */
  public static void setUri(String uri) {
    Runner.uri = uri;
  }

  /**
   * @return the output directory for the extracted sentiment
   */
  public static String getOutputDir() {
    return outputDir;
  }

  /**
   * @param outputDir the output directory to set for extracted sentiment
   */
  public static void setOutputDir(String outputDir) {
    Runner.outputDir = outputDir;
  }

  /**
   * @return the output format for which we wish sentiment
   * to be serialized.
   */
  public static String getOutputFormat() {
    return outputFormat;
  }

  /**
   * @param outputFormat the output format to serialize the sentiment
   */
  public static void setOutputFormat(String outputFormat) {
    Runner.outputFormat = outputFormat;
  }

}
