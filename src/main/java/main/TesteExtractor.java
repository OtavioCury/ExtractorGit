package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.json.JSONObject;

public class TesteExtractor {
	public static void main(String[] args) {
		String basePath1 = "Teste";
		String basePath2 = "Teste";
		String answerFile1 = "Teste";
		String answerFile2 = "Teste";
		List<String> repos1 = new ArrayList<String>();
		repos1.add("Teste1");
		repos1.add("Teste2");
		List<String> repos2 = new ArrayList<String>();
		repos2.add("Teste1");
		repos2.add("Teste2");
		JSONObject jsonObjectCHA = new JSONObject();
		JSONObject jsonObjectRMCA = new JSONObject();
		JSONObject jsonObjectProject = new JSONObject();
		JSONObject aux = new JSONObject();
		
		jsonObjectCHA.put("base_path", basePath1);
		jsonObjectCHA.put("repos", repos1);
		jsonObjectCHA.put("answer_file", answerFile1);
		jsonObjectRMCA.put("base_path", basePath2);
		jsonObjectRMCA.put("repos", repos2);
		jsonObjectRMCA.put("answer_file", answerFile2);
		
		aux.put("CHA", jsonObjectCHA);
		aux.put("RMCA", jsonObjectRMCA);
		jsonObjectProject.put("projects", aux);
		
		System.out.println(jsonObjectProject.toString());
		String[] array = new String[1];
		array[0] = jsonObjectProject.toString();
		try {
			Extractor.main(array);
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
}
