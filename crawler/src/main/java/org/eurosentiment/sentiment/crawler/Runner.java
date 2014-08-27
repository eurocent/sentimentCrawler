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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
  public static void main(String[] args) {
    Options options = new Options();
    Option help = new Option( "help", "print this message" );
    @SuppressWarnings("static-access")
    Option urlArg = OptionBuilder.withArgName( "url" )
    .hasArg()
    .withDescription( "run sentiment extraction on this URL" )
    .create( "url" );
    @SuppressWarnings("static-access")
    Option outputDirArg = OptionBuilder.withArgName( "output_dir" )
    .hasArg()
    .withDescription( "output directory for extracted s, p, o sentiments" )
    .create( "outputDir" );
    @SuppressWarnings("static-access")
    Option outputFormatArg = OptionBuilder.withArgName( "output_serialization" )
    .hasArg()
    .withDescription( "output serialization for extracted s, p, o sentiments "
        + "(one of 'turtle', 'ntriples', 'rdfxml', 'nquads', 'trix' or 'json')" )
        .create( "outputFormat" );

    options.addOption(help);
    options.addOption(urlArg);
    options.addOption(outputDirArg);
    options.addOption(outputFormatArg);
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
