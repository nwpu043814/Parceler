package com.lzh.compiler.parceler.processor;

import com.lzh.compiler.parceler.annotation.Arg;
import com.lzh.compiler.parceler.annotation.Dispatcher;
import com.lzh.compiler.parceler.processor.factory.ActivityFactory;
import com.lzh.compiler.parceler.processor.factory.CommenFactory;
import com.lzh.compiler.parceler.processor.factory.DispatcherFactory;
import com.lzh.compiler.parceler.processor.factory.FragmentFactory;
import com.lzh.compiler.parceler.processor.factory.ReceiverFactory;
import com.lzh.compiler.parceler.processor.factory.ServiceFactory;
import com.lzh.compiler.parceler.processor.model.Constants;
import com.lzh.compiler.parceler.processor.model.ElementParser;
import com.lzh.compiler.parceler.processor.model.FieldData;
import com.lzh.compiler.parceler.processor.util.UtilMgr;
import com.lzh.compiler.parceler.processor.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class ParcelerCompiler extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Map<TypeElement, ElementParser> parserMap = parseArgElement(roundEnv);
            parseDispatcherElement (roundEnv,parserMap);
        } catch (ParcelException e) {
            e.printStackTrace();
            error(e.getEle(),e.getMessage());
            return true;
        }
        return false;
    }

    /**
     * Parse elements with {@link Dispatcher}
     * @throws ParcelException
     */
    private void parseDispatcherElement(RoundEnvironment roundEnv, Map<TypeElement, ElementParser> parserMap) throws ParcelException{
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Dispatcher.class);
        DispatcherFactory factory;
        TypeElement type = null;
        try {
            for (Element ele : elements) {
                type = (TypeElement) ele;
                if (!Utils.checkClassValid(type)) {
                    continue;
                }
                if (Utils.isSuperClass(type, Constants.CLASS_NAME_ACTIVITY)) {
                    factory = new ActivityFactory();
                } else if (Utils.isSuperClass(type, Constants.CLASS_NAME_FRAGMENT)
                        || Utils.isSuperClass(type, Constants.CLASS_NAME_V4_FRAGMENT)) {
                    factory = new FragmentFactory();
                } else if (Utils.isSuperClass(type, Constants.CLASS_NAME_RECEIVER)) {
                    factory = new ReceiverFactory();
                } else if (Utils.isSuperClass(type, Constants.CLASS_NAME_SERVICE)) {
                    factory = new ServiceFactory();
                } else {
                    factory = new CommenFactory();
                }
                factory.setType(type);
                factory.setFieldList(parserMap.get(type) == null ? new ArrayList<FieldData>() : parserMap.get(type).getList());
                factory.generateCode();
            }
        } catch (ParcelException e) {
            throw e;
        } catch (Throwable e) {
            throw new ParcelException(String.format("Parceler compiler generated java files failed: %s,%s", type, e.getMessage()),e ,type);
        }
    }

    /**
     * Parse elements with {@link Arg}
     * @throws ParcelException
     */
    private Map<TypeElement,ElementParser> parseArgElement(RoundEnvironment roundEnv) throws ParcelException{
        Map<TypeElement,ElementParser> parserMap = new HashMap<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Arg.class);
        TypeElement type = null;
        try {
            // parse and get data from elements
            for (Element ele : elements) {
                type = (TypeElement) ele.getEnclosingElement();
                if (!Utils.checkClassValid(type) || parserMap.containsKey(type)) {
                    continue;
                }
                parserMap.put(type,ElementParser.parse(type));
            }
            // generate injector class
            Set<TypeElement> keys = parserMap.keySet();
            for (TypeElement key : keys) {
                parserMap.get(key).generateClass();
            }
        } catch (ParcelException e) {
            throw e;
        } catch (Throwable e) {
            throw new ParcelException(String.format("Parceler compiler generated java files failed: %s,%s", type, e.getMessage()),e,type);
        }
        return parserMap;
    }

    /**
     * compiler output method,when compiler occurs exception.should be notice here.
     *
     * @param element Element of class who has a exception when compiled
     * @param message The message should be noticed to user
     * @param args args to inflate message
     */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void note(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Arg.class.getCanonicalName());
        set.add(Dispatcher.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        UtilMgr.getMgr().init(processingEnv);
    }
}
