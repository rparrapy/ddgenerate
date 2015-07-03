package py.gov.datos;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by rparra on 24/6/15.
 */
public class CsvToDataPackageConverter implements FileConverter {
    private final Logger LOG = LoggerFactory.getLogger(CsvToDataPackageConverter.class);
    private final String SPLIT_BY = ";";
    private static final Map<String, List<String>> LANG_REFERENCES;
    private static final Map<String, String> XSD_TO_TYPE;
    private List<Integer> removedColumns = new ArrayList<>();

    static {
        LANG_REFERENCES = new HashMap<>();

        List<String> refEs = Arrays.asList(new String[]{": ESPAÑOL",
                ": ESPAÑOL,", ": Español"});
        LANG_REFERENCES.put("es", refEs);

        List<String> refEn = Arrays.asList(new String[]{": INGLES",
                ": INGLES,", ": Inglés", ": Ingles"});
        LANG_REFERENCES.put("en", refEn);

        XSD_TO_TYPE = new HashMap<>();
        XSD_TO_TYPE.put("xsd:string", "string");
        XSD_TO_TYPE.put("xsd:positiveInteger", "integer");
        XSD_TO_TYPE.put("xsd:boolean", "boolean");

    }

    @Override
    public List<File> convert(List<File> files, String path, Map<String, String> params) {
        List<File> result = new ArrayList<>();

        File schemaDir = new File(path + "schema/");
        schemaDir.mkdir();

        for (File file : files) {
            try {
                if (file.getName().equals("Clases.csv")) {
                    result.add(makeIndex(file, path, params));
                } else {
                    result.add(makePage(file, path, params));
                }

            }catch(JSONException e){
                LOG.error("Can not parse JSON.");
            }
        }
        return result;
    }

    private File makeIndex(File file, String path, Map<String, String> params) throws JSONException {

        List<List<String>> clazzes = getClazzList(file, path, params);
        JSONArray content = new JSONArray();

        int cont = 0;
        for (List<String> record : clazzes) {
            OrderedJSONObject clazz = new OrderedJSONObject();
            //clazz.put("index", ++cont);
            clazz.put("class", record.get(0));
            clazz.put("name", record.get(1));
            clazz.put("description", record.get(2));
            content.add(clazz);
        }

        File result = null;
        try {
            result = writeToFile("index", path + "app/views/", StringEscapeUtils.unescapeJava(content.toString(4)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<List<String>> getAttributeList(File file, String path, Map<String, String> params) {
        String lang = params.get("language");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.isEmpty()) {
                fr.close();
                br.close();
                return null;
            }

            List<String> header = new ArrayList<>();
            List<List<String>> table = new ArrayList<>();
            int cont = 0;

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(CustomStringEscapeUtils.unescapeCsv(line)
                        .replace("\"", "").split(SPLIT_BY)));

                if (elems.size() > 1) {
                    if (cont == 0) {
                        elems = localizeTable(lang, elems, true);
                        header.addAll(elems);
                    } else {
                        elems = localizeTable(lang, elems, false);
                        while (elems.size() < header.size()) {
                            elems.add("");
                        }
                        //System.out.println(elems);
                        //System.out.println(elems.size());
                        table.add(elems);
                    }
                    cont++;
                }
            }

            br.close();
            fr.close();
            return table;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File makePage(File file, String path, Map<String, String> params) throws JSONException {
        List<List<String>> attrs = getAttributeList(file, path, params);
        JSONObject content = new JSONObject();
        JSONArray fields = new JSONArray();

        for (List<String> record : attrs) {
            OrderedJSONObject clazz = new OrderedJSONObject();
            clazz.put("name", record.get(0));
            clazz.put("title", record.get(3));
            clazz.put("example", record.get(5));
            clazz.put("type", XSD_TO_TYPE.get(record.get(6)));
            clazz.put("restrictions", record.get(8));
            clazz.put("description", record.get(10));
            fields.add(clazz);
        }

        content.put("fields", fields);

        String subpath = "app/views/" + getFileName(file.getName().substring(0, file.getName().length() - 4)) + "/";
        //System.out.println(StringEscapeUtils.unescapeJava(content.toString(4)));
        File result = null;
        try {
            result = writeToFile("schema", path + subpath, StringEscapeUtils.unescapeJava(content.toString(4)));
       } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }


    private List<List<String>> getClazzList(File file, String path, Map<String, String> params) {
        String lang = params.get("language");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String reference = br.readLine().replace("\"", "")
                    .replace(";", " ");
            String tableTitle = br.readLine().replace("\"", "")
                    .replace(";", "");

            boolean st = false;
            List<String> headerOne = new ArrayList<>();
            List<List<String>> tableOne = new ArrayList<>();
            List<String> headerTwo = new ArrayList<>();
            List<List<String>> tableTwo = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            while ((line = CustomStringEscapeUtils.unescapeCsv(br.readLine())) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line
                        .replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty()) {
                    if (elems.size() >= 2 && elems.get(1).equals("es")) {
                        elems = localizeTable(lang, elems, true);
                        headerOne.addAll(elems);
                        continue;
                    }

                    if (elems.get(0).equals("Clases")) {
                        elems = localizeTable(lang, elems, true);
                        headerTwo.addAll(elems);
                        st = true;
                        continue;
                    }
                    elems = localizeTable(lang, elems, false);
                    if (st) {
                        if (elems.size() > 1) {
                            while (elems.size() < headerTwo.size()) {
                                elems.add("");
                            }
                            tableTwo.add(elems);
                            String name = elems.get(0);
                            name = name.toLowerCase().replace("á", "a")
                                    .replace("é", "e").replace("í", "i")
                                    .replace("ó", "o").replace("ú", "u")
                                    .replace(" ", "_");
                            urls.add("def/" + name);
                        }
                    } else {
                        if (elems.size() > 1) {
                            while (elems.size() < headerOne.size()) {
                                elems.add("");
                            }
                            tableOne.add(elems);
                        }
                    }
                }
            }

            return tableTwo;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Crea un archivo de salida y escribe el contenido correspondiente.
     *
     * @param name    el nombre del archivo a escribir.
     * @param path    la ruta donde se encuentra el archivo.
     * @param content el contenido a escribir en el archivo.
     * @return el archivo creado.
     * @throws java.io.IOException
     */
    private File writeToFile(String name, String path, String content) throws IOException {
        File outputFile = new File(path + name + ".json");
        //borramos la version anterior
        if(outputFile.isFile()) outputFile.delete();
        if (new File(path).isDirectory() && outputFile.createNewFile()) {
            System.out.println("Writing file: " + path + name + ".json");
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF-8"));
            out.write(content);
            out.flush();
            out.close();
        } else {
            LOG.error("Can not create output file: " + path + name + ".json");
        }
        return outputFile;

    }

    /**
     * Elimina de la lista de elementos aquellos que corresponden a un idioma
     * diferente al indicado.
     * <p/>
     * Si se trata del header, almacena en una lista los índices de las columnas
     * a eliminar para las siguientes filas.
     *
     * @param lang   el idioma seleccionado.
     * @param elems  la lista de elementos correspondiente a una fila del .csv
     * @param header indica si la fila es o no el header de la tabla.
     * @return la lista con las cadenas correspondientes al idioma seleccionado.
     */
    private List<String> localizeTable(String lang, List<String> elems,
                                       boolean header) {
        if (header) {
            elems = localizeHeader(lang, elems);
        }
        for (Integer j : removedColumns) {
            if (j.intValue() < elems.size())
                elems.remove(j.intValue());
        }

        List<String> results = new ArrayList<String>();
        for (String elem : elems) {
            elem = elem.replace("@", "@@");
            results.add(elem);
        }
        return results;
    }

    /**
     * Elimina de la lista de elementos correspondiente al header de la tabla,
     * aquellos que corresponden a un idioma diferente al indicado.
     * <p/>
     * Además, almacena los índices de las columnas a eliminar para las
     * siguientes filas.
     *
     * @param lang  el idioma seleccionado.
     * @param elems la lista de elementos correspondiente a una fila del .csv
     * @return la lista con las cadenas correspondientes al idioma seleccionado.
     */
    private List<String> localizeHeader(String lang, List<String> elems) {
        removedColumns = new ArrayList<>();
        List<String> result = new ArrayList<>();
        int i = 0;
        for (String elem : elems) {
            for (String candidateLang : LANG_REFERENCES.keySet()) {
                for (String toRemove : LANG_REFERENCES.get(candidateLang)) {
                    if (elem.contains(toRemove) || elem.equals(candidateLang)) {
                        elem = elem.replace(toRemove, "");
                        if (!candidateLang.equals(lang)) {
                            removedColumns.add(i);
                        }
                    }
                }
            }
            i++;
            result.add(elem);
        }
        removedColumns = new ArrayList<>(new HashSet<>(removedColumns));
        Collections.sort(removedColumns, Collections.reverseOrder());
        return result;
    }

    private String getFileName(String title){
        return Normalizer
                .normalize(title, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll(" +", "-")
                .toLowerCase();
    }
}
