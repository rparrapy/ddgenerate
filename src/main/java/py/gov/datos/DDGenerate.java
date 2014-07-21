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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class DDGenerate {
    private final Logger LOG = LoggerFactory.getLogger(DDGenerate.class);

    @Parameter(description = "Files")
    private List<String> files = new ArrayList<String>();

    @Parameter(names = {"-o", "--output"}, description = "Destination path")
    private String out = "";

    @Parameter(names = {"-f", "--format"}, description = "Desired output format")
    private String format = "html";

    private FileConverterFactory f = new FileConverterFactory();


    private static final Map<String, List<FileConverterType>> chainMap;

    static {
        chainMap = new HashMap<String, List<FileConverterType>>();
        chainMap.put("html", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV, FileConverterType.CSV_TO_HTML}));
        chainMap.put("csv", Arrays.asList(new FileConverterType[]
                {FileConverterType.XLS_TO_CSV}));
    }

    public void generate() {
        List<FileConverterType> chain = chainMap.get(format);
        for (String filename : files) {
            File file = new File(filename);
            List<File> convertee = new ArrayList<File>();
            convertee.add(file);
            generate(convertee, chain);
        }
        LOG.info("Success!");
    }

    private void generate(List<File> files, List<FileConverterType> chain) {
        if (!chain.isEmpty()) {
            FileConverter converter = f.getFileConverter(chain.get(0));
            List<File> results = converter.convert(files, out);
            generate(results, chain.subList(1, chain.size()));
        }
    }


    public static void main(String[] args) {
        DDGenerate generator = new DDGenerate();
        new JCommander(generator, args);
        generator.generate();
    }
}
