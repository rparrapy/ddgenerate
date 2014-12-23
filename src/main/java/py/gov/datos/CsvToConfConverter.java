package py.gov.datos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvToConfConverter implements FileConverter {

	private final Logger LOG = LoggerFactory
			.getLogger(CsvToConfConverter.class);
	private final String SPLIT_BY = ";";

	private Map<String, String> equivalencias = new HashMap<String, String>();
	private Map<String, String> tipos = new HashMap<String, String>();
	private Map<String, List<String>> clasesAnidadas = new HashMap<String, List<String>>();

	@Override
	public List<File> convert(List<File> files, String path,
			Map<String, String> params) {
		for (File file : files) {
			if (file.getName().equals("Clases.csv")) {

			} else {
				processClass(file);
			}
		}

		String content = generateContent();
		try {
			File result = writeToFile("dncp", path, content);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Can not create output file");
		}
		return files;
	}

	private String generateContent() {
		String result = "";
		for (String k : equivalencias.keySet()) {
			result += k + ".nombre=" + equivalencias.get(k) + "\n";
			result += k + ".tipo=" + tipos.get(k) + "\n";
		}

		result += "\n\n\n";

		for (String k : clasesAnidadas.keySet()) {
			String values = k + "clases." + "=[";
			for (String clazz : clasesAnidadas.get(k)) {
				values += clazz + ",";
			}
			values += "]\n";
			result += values;
		}

		return result;
	}

	private void processClass(File file) {
		FileReader fr = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			String headerLine = br.readLine();
			if (headerLine == null || headerLine.isEmpty()) {
				fr.close();
				br.close();
				return;
			}
			String clazzTitle = headerLine.replace("\"", "").replace(";", "")
					.split("clase")[1].trim();
			String clazzName = getClassName(clazzTitle);
			ArrayList<String> columns = new ArrayList<>(Arrays.asList(br
					.readLine().replace("\"", "").split(SPLIT_BY)));
			clasesAnidadas.put(clazzName, new ArrayList<String>());
			//System.out.println(clazzName);
			while ((line = br.readLine()) != null) {
				List<String> elems = new ArrayList<>(Arrays.asList(line
						.replace("\"", "").split(SPLIT_BY)));
				if (!elems.isEmpty() && elems.get(0).length() > 0) {
					while (elems.size() < columns.size()) {
						elems.add("");
					}
					equivalencias.put(clazzName + "." + elems.get(0), elems.get(1));
					tipos.put(clazzName + "." + elems.get(0), getClassName(elems.get(8)));
					if (Character.isUpperCase(elems.get(8).charAt(0))) {
						clasesAnidadas.get(clazzName).add(elems.get(0));
					}
				}
			}
			fr.close();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Crea un archivo de salida y escribe el contenido correspondiente.
	 * 
	 * @param name
	 *            el nombre del archivo a escribir.
	 * @param path
	 *            la ruta donde se encuentra el archivo.
	 * @param content
	 *            el contenido a escribir en el archivo.
	 * @return el archivo creado.
	 * @throws java.io.IOException
	 */
	private File writeToFile(String name, String path, String content)
			throws IOException {
		File outputFile = new File(path + "conf/" + name + ".conf");
		outputFile.delete();
		if (outputFile.createNewFile()) {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "UTF-8"));
			out.write(content);
			out.flush();
			out.close();
		} else {
			LOG.error("Can not create output file");
		}
		return outputFile;

	}

	private String getClassName(String title){
		return Normalizer
				.normalize(title, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "").replaceAll(" +", "_")
				.toLowerCase();
	}

}
