package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.json.JSONObject;

public class TesteExtractor {
	public static void main(String[] args) {
		String basePath1 = "/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/testeJSON/fastlane.xlsx";
		String basePath2 = "/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/testeJSON/activeadmin.xlsx";
		String answerFile1 = "/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/testeJSON/codivision1.xlsx";
		String answerFile2 = "/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/testeJSON/codivision2.xlsx";
		List<String> repos1 = new ArrayList<String>();
		repos1.add("/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/projetos/fastlane/fastlane/.git");
		repos1.add("/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/projetos/gatsbyjs/gatsby/.git");
		List<String> repos2 = new ArrayList<String>();
		repos2.add("/media/lost/e04b3034-2506-41c9-a1d4-e3d38fe04256/otavio/projetos/activeadmin/activeadmin/.git");
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
