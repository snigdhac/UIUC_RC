package edu.illinois.cs.cogcomp;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
//import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by snigdha on 8/5/16.
 */
public class ReadData {

        private AnnotatorService annotator=null;

        private void initializeAnnotator() throws IOException, AnnotatorException {
            /* OPTIONAL STEP:
         * By default the pipeline will load all its annotators which will take up a LOT of memory (~ 8 GB).
         * Use the following properties to disable the annotators you might not need.
         * For now, we're keeping just the POS tagger. */
            Properties nonDefaultProp = new Properties();
            nonDefaultProp.put(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
            nonDefaultProp.put(PipelineConfigurator.USE_LEMMA.key, Configurator.TRUE);
            nonDefaultProp.put(PipelineConfigurator.USE_NER_CONLL.key, Configurator.TRUE);
            nonDefaultProp.put(PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE);
            nonDefaultProp.put(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.TRUE);
            nonDefaultProp.put(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE);
            nonDefaultProp.put(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.FALSE);
            nonDefaultProp.put(PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE);
            nonDefaultProp.put(PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE);

            // Create the AnnotatorService object
            this.annotator = IllinoisPipelineFactory.buildPipeline(new ResourceManager(nonDefaultProp));
        }

        public static void main(String[] args) throws IOException, AnnotatorException {

            ReadData corpus = new ReadData();

            corpus.initializeAnnotator();

//            String subdir = "/home/snigdha/Documents/RC/Data/remedia/Downloaded/";
            String subdir = "/Users/Snigdha/Documents/Work/UIUC/RC/Data/remedia/Downloaded/";
            int level = 2;
            String inputfile = subdir + "level"+level+"/org/rm2-3.txt";
            String outputfile = subdir +"../Processed/rm2-1.processed";

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
            bw.write("meta\tparaId/qid\tsent\tword\tlemm,\tPOS");
            bw.newLine();

            BufferedReader br = new BufferedReader(new FileReader(inputfile));

            br.readLine(); // header
            br.readLine(); // next line is new line
            String storyTitle = br.readLine(); // title of the text
            br.readLine(); // next line is new line
            String line = br.readLine(); // contains the date, time as well as the first para
            String locDateMeta = "";
            if(line.matches("( *)\\((.*)\\) - (.*)")){ // format is (#locdate) - #textContinues
                locDateMeta = line.substring(0, line.indexOf(") - ") + 1);
                line = line.substring(line.indexOf(") - ") + 3); // firstPara: format is (location, date) - paragraph begins here. so we have +3
            }
            else
                System.out.println("I was expecting a format of (#locdate) - #textContinues. Could not find location, time metadata in file:" +inputfile);
;
            int paraId = 0;

            // process the story
            while(line!=null && !line.startsWith("1.")){
                if(line.trim().length()!=0) {
                    processText(corpus.annotator, line, bw, "text", paraId);
                }
                line = br.readLine();
                paraId +=1;
            }

            // process the questions
            while(line!=null){
                if(line.trim().length()!=0) {
                    int qid = Integer.parseInt(line.substring(0,line.indexOf(".")));
                    String ques =line.substring(line.indexOf(".")+1).trim();
                    processText(corpus.annotator, ques, bw, "ques", qid);
                }
                line = br.readLine();
            }
            br.close();
            bw.close();
    }

    private static void processText(AnnotatorService annotator, String text, BufferedWriter bw, String metaInfo, int paraId) throws AnnotatorException, IOException {

        // Create a new TextAnnotation object. This will tokenize and split the sentences
        // (it will create the TOKENS and SENTENCE views).
        TextAnnotation ta = annotator.createBasicTextAnnotation("corpusID", "textID", text);


        // Add the POS View (run the POS tagger)
        annotator.addView(ta, ViewNames.POS);
        annotator.addView(ta, ViewNames.LEMMA);
        annotator.addView(ta, ViewNames.SHALLOW_PARSE);
        annotator.addView(ta, ViewNames.NER_CONLL);

        // Accessing the view
        View posView = ta.getView(ViewNames.POS);
        View lemmView = ta.getView(ViewNames.LEMMA);
        View shallowParseView = ta.getView(ViewNames.SHALLOW_PARSE);
        View nerView = ta.getView(ViewNames.NER_CONLL);
        View sentView = ta.getView(ViewNames.SENTENCE);

        // print all phrases
        for(int sentId=0;sentId<sentView.getConstituents().size();sentId++) {
            Constituent sent1 = sentView.getConstituents().get(sentId);
            List<Constituent> thisSentLemma = lemmView.getConstituentsCovering(sent1);
            List<Constituent> thisSentPOS = posView.getConstituentsCovering(sent1);
            List<Constituent> thisSentPhrases = shallowParseView.getConstituentsCovering(sent1);
            System.out.println(metaInfo + "\t" + paraId + "\t" + sentId + "\t");
            for(Constituent c:thisSentPOS){
                System.out.println("\tpos:"+c.getSurfaceForm()+":"+c.getLabel());
                for(Constituent c1:nerView.getConstituentsCovering(c))
                    System.out.println("\t\tner:"+c1.getSurfaceForm()+":"+c1.getLabel());

            }


//            for(Constituent c:thisSentPhrases) {
//                if(c.getLabel().equals("NP"))
//                System.out.println("\tphrase:"+c.getSurfaceForm()+":"+c.getLabel());
//                List<Constituent> nerInThisPhrase = nerView.getConstituentsCovering(c);
//                for(Constituent c1 :nerInThisPhrase)
//                    System.out.println("\t\tNER:"+c1.getSurfaceForm()+":"+c1.getLabel());
//            }
            System.out.println();
            System.out.println();
        }
//        System.out.println(posView);

//         Accessing the Constituents of the View
//        for (Constituent c : nerCoNLLView.getConstituents()) {
//            System.out.println(metaInfo + "\t" + paraId + "\t"+"Word: " + c.getAttributeKeys()+"\t"+c.getTokenizedSurfaceForm()+"\t"+ c.getSurfaceForm() + "\tPOS: " + c.getLabel());
//        }
//        System.out.println();
//
        // Accessing the Constituents per sentence
//        View sentView = ta.getView(ViewNames.SENTENCE);

//        View tokView = ta.getView(ViewNames.TOKENS);

//        List<Constituent> temp = tokView.getConstituents();
//        for(Constituent c: temp){
//            System.out.println(metaInfo + "\t" + paraId + "\t" + c.getSentenceId()+"\t"+c.getSurfaceForm()+"\t"+lemmView.getConstituentsCovering(c).get(0).getLabel()+"\t"+posView.getConstituentsCovering(c).get(0).getLabel());
//        }


        for(int sentId=0;sentId<sentView.getConstituents().size();sentId++) {
            Constituent sent1 = sentView.getConstituents().get(sentId);
            List<Constituent> thisSentLemma = lemmView.getConstituentsCovering(sent1);
            List<Constituent> thisSentPOS = posView.getConstituentsCovering(sent1);
            for(int wid = 0;wid<thisSentLemma.size();wid++){
                Constituent c = thisSentLemma.get(wid);
                bw.write(metaInfo + "\t" + paraId + "\t" + sentId + "\t" + c.getSurfaceForm() + "\t" + c.getLabel()+"\t"+thisSentPOS.get(wid).getLabel());
                bw.newLine();
            }
        }
    }
}
