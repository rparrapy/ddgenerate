package py.gov.datos;

/*
 * @author	Rodrigo Parra	
 * @copyright	2014 Governance and Democracy Program USAID-CEAMSO
 * @license 	http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * USAID-CEAMSO
 * Copyright (C) 2014 Governance and Democracy Program
 * http://ceamso.org.py/es/proyectos/20-programa-de-democracia-y-gobernabilidad
 * 
----------------------------------------------------------------------------
 * This file is part of the Governance and Democracy Program USAID-CEAMSO,
 * is distributed as free software in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. You can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License version 2 as published by the 
 * Free Software Foundation, accessible from <http://www.gnu.org/licenses/> or write 
 * to Free Software Foundation (FSF) Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02111-1301, USA.
 ---------------------------------------------------------------------------
 * Este archivo es parte del Programa de Democracia y Gobernabilidad USAID-CEAMSO,
 * es distribuido como software libre con la esperanza que sea de utilidad,
 * pero sin NINGUNA GARANTÍA; sin garantía alguna implícita de ADECUACION a cualquier
 * MERCADO o APLICACION EN PARTICULAR. Usted puede redistribuirlo y/o modificarlo 
 * bajo los términos de la GNU Lesser General Public Licence versión 2 de la Free 
 * Software Foundation, accesible en <http://www.gnu.org/licenses/> o escriba a la 
 * Free Software Foundation (FSF) Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02111-1301, USA.
 */

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Clase principal ejecutable del convertidor de diccionario de datos.
 */
public class DDGenerate {
    private final Logger LOG = LoggerFactory.getLogger(DDGenerate.class);

    @Parameter(description = "Files")
    private List<String> files = new ArrayList<String>();

    @Parameter(names = {"-o", "--output"}, description = "Destination path")
    private String out = "";

    @Parameter(names = {"-f", "--format"}, description = "Desired output format")
    private List<String> formats = new ArrayList<>(Arrays.asList("html"));

    @Parameter(names = {"-l", "--lang"}, description = "Desired output language")
    private String lang = "es";

    private FileConverterFactory f = new FileConverterFactory();


    private static final Map<String, List<FileConverterType>> chainMap;

    static {
        chainMap = new HashMap<String, List<FileConverterType>>();
        chainMap.put("html", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV, FileConverterType.CSV_TO_HTML}));
        chainMap.put("csv", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV}));
        chainMap.put("owl", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV, FileConverterType.CSV_TO_OWL}));
        chainMap.put("jsonld", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV, FileConverterType.CSV_TO_JSONLD}));
    }

    /**
     * Genera los archivos convertidos en el directorio de salida.
     *
     * Por cada formato de salida, lee los pasos necesarios para realizar la conversión.
     * Ej: "html" -> XLS_TO_CSV + CSV_TO_HTML
     *
     * El primer paso recibe como entrada el archivo Excel del diccionario de datos.
     * Cada paso siguiente recibe como entrada los archivos generados por el paso anterior.
     *
     * @see py.gov.datos.FileConverterType
     */
    public void generate() {
    	if(!out.endsWith("/")){
    		out = out + "/";
    	}
        cleanOutputDir("def/", "csv/", "contexts/");
        Map<String, String> params = new HashMap<>();
        params.put("language", lang);

        for(String format: formats){
            List<FileConverterType> chain = chainMap.get(format);
            for (String filename : files) {
                File file = new File(filename);
                List<File> convertee = new ArrayList<File>();
                convertee.add(file);
                generate(convertee, chain, params);
            }
            cleanOutputDir("csv/");
        }
    }

    /**
     * Convierte una lista de archivos a un formato determinado por el primer elemento
     * de la lista de pasos que recibe como parámetro.
     *
     * Este método se ejecuta de forma recursiva hasta vacíar la lista de pasos de conversión.
     *
     * @param files la lista de archivos a convertir.
     * @param chain lista de pasos, siendo cada paso una conversión de un formato a otro.
     * @param params parámetros de configuración, por ejemplo, el idioma de salida.
     */
    private void generate(List<File> files, List<FileConverterType> chain, Map<String, String> params) {
        if (!chain.isEmpty()) {
            FileConverter converter = f.getFileConverter(chain.get(0));
            List<File> results = converter.convert(files, out, params);
            generate(results, chain.subList(1, chain.size()), params);
        }
    }

    /**
     * Elimina recursivamente una lista de directorios.
     * @param dirList la lista de directorios a eliminar.
     */
    private void cleanOutputDir(String... dirList){
        for(String dirName: dirList){
            File dir = new File(out + dirName);
            if(dir.exists()){
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    LOG.error("Can not delete existing directory");
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        DDGenerate generator = new DDGenerate();
        new JCommander(generator, args);
        generator.generate();
    }
}
