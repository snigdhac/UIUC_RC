package edu.illinois.cs.cogcomp;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

public class SampleCoref {

        public static void main(String[] args) throws Exception {

            AnnotatorService annotator = CuratorFactory.buildCuratorClient();
            TextAnnotation ta = annotator.createBasicTextAnnotation("sample", "1", "Mary is eating an apple. She likes it.");

            annotator.addView(ta, ViewNames.POS);
            annotator.addView(ta, ViewNames.LEMMA);
            annotator.addView(ta, ViewNames.NER_CONLL);
            annotator.addView(ta, ViewNames.PARSE_STANFORD);
            annotator.addView(ta, ViewNames.SRL_VERB);
            annotator.addView(ta, ViewNames.COREF);

            System.out.println("pos:");
            System.out.println(ta.getView(ViewNames.POS));
            System.out.println("lemma:");
            System.out.println(ta.getView(ViewNames.LEMMA));
            System.out.println("ner:");
            System.out.println(ta.getView(ViewNames.NER_CONLL));
            System.out.println("parse:");
            System.out.println(ta.getView(ViewNames.PARSE_STANFORD));
            System.out.println("srl:");
            System.out.println(ta.getView(ViewNames.SRL_VERB));
            System.out.println("coref:");
            System.out.println(ta.getView(ViewNames.COREF));

    }
}
