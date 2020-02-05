package main;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import modelo.Author;
import modelo.Constants;
import modelo.Levenshtein;
import modelo.ModeloOtavio;
import modelo.OperationFile;
import modelo.OperationType;
import modelo.Revision;


public class Extractor {

	static String caminhoGit = "pathToGit";
	static String caminhoRespostas = "pathToFile.xlsx";
	static String caminhoSaida = "pathToFileOut.xlsx";
	
	private static String[] columns = {"Nome", "Email", "Arquivo", "Familiaridade", 
			"Data", "Adds", "Dels", "Mods", "Cond", "Montante","DataUltima", "NumCommits","QuantDias", 
			"NumDevs", "Blame", "TotalLinhas", "PrimeiroAutor", "DOA", "Mantenedor", "QuantArquivos", "DiasEntreCommits"};

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		if (args != null && args[0] != null) {
			caminhoGit = args[0];
		}
		if (args != null && args[1] != null) {
			caminhoRespostas = args[1];
		}
		if (args != null && args[2] != null) {
			caminhoSaida = args[2];
		}
		Git git;
		Repository repository;
		Instant start = Instant.now();
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("codiVision");
		Row headerRow = sheet.createRow(0);
		for(int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
		}

		List<String> projetoArquivos = new ArrayList<String>();
		List<ModeloOtavio> lista = new ArrayList<ModeloOtavio>();
		projetoAnalisado(lista, projetoArquivos);
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		int rowNum = 1;
		Queue<ModeloOtavio> fila = new LinkedList<ModeloOtavio>();
		for(ModeloOtavio modelo : lista) {
			fila.add(modelo);
		}
		System.out.println("====== Analisando projeto =======");
		git = Git.open(new File(caminhoGit));
		repository = git.getRepository();
		List<Revision> commits = getRevisions(projetoArquivos, git, repository);
		//					emailsArquivo(commits);
		while (!fila.isEmpty()) {
			ModeloOtavio modelo = fila.poll();
			Date dataUltima = null;
			Row row = sheet.createRow(rowNum++);
			int adds = -1;
			int dels = -1;
			int mods = -1;
			int conds = -1;
			int numDevs = -1;
			int montante = -1;
			int numCommits = -1;
			int diffDias = -1;
			int avgCommits = -1;
			int numeroArquivos = numeroOtherFile(commits, modelo.getEmail(), modelo.getNome());
			double doa = -1.0;
			boolean primeiroAutor = false;
			BlameTotal blameTotal = null;
			if (commits != null) {
				blameTotal = blameTotal(modelo.getNome(), modelo.getArquivo(), repository);
				dataUltima = lastModify(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				adds = somaAdd(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				dels = somaDel(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				mods = somaMod(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				conds = somaCond(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				montante = somaMontante(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				numCommits = numCommits(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				avgCommits = avgDaysCommits(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome(), numCommits);
				numDevs = numberModsDevelopers(commits, modelo.getEmail(), modelo.getArquivo(), dataUltima, modelo.getNome());
				primeiroAutor = primeiroAutor(commits, modelo.getEmail(), modelo.getArquivo(), modelo.getNome());
				doa = calculaDoa(numCommits, numDevs, primeiroAutor);
			}
			row.createCell(0).setCellValue(modelo.getNome());
			row.createCell(1).setCellValue(modelo.getEmail());
			row.createCell(2).setCellValue(modelo.getArquivo());
			row.createCell(3).setCellValue(modelo.getFamiliaridade());
			row.createCell(4).setCellValue(formato.format(modelo.getData()));
			row.createCell(5).setCellValue(adds);
			row.createCell(6).setCellValue(dels);
			row.createCell(7).setCellValue(mods);
			row.createCell(8).setCellValue(conds);
			row.createCell(9).setCellValue(montante);
			if (dataUltima != null) { 
				String dataFormatada = formato.format(dataUltima);
				row.createCell(10).setCellValue(dataFormatada);
				long diff = modelo.getData().getTime() - dataUltima.getTime();
				diffDias = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			}else {
				row.createCell(10).setCellValue(dataUltima);
			}
			row.createCell(11).setCellValue(numCommits);
			row.createCell(12).setCellValue(diffDias);
			row.createCell(13).setCellValue(numDevs);
			if (blameTotal != null) {
				row.createCell(14).setCellValue(blameTotal.blame);
				row.createCell(15).setCellValue(blameTotal.total);
			}else {
				row.createCell(14).setCellValue(-1);
				row.createCell(15).setCellValue(-1);
			}
			if (primeiroAutor) {
				row.createCell(16).setCellValue(1);
			}else {
				row.createCell(16).setCellValue(0);
			}
			row.createCell(17).setCellValue(doa);
			if (modelo.getFamiliaridade() >= 3) {
				row.createCell(18).setCellValue(1);
			}else {
				row.createCell(18).setCellValue(0);
			}
			row.createCell(19).setCellValue(numeroArquivos);
			row.createCell(20).setCellValue(avgCommits);
		}
		commits = null;

		FileOutputStream fileOut;
		fileOut = new FileOutputStream(caminhoSaida);
		workbook.write(fileOut);
		fileOut.close();

		try {
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Instant finish = Instant.now();
		long minutos = Duration.between(start, finish).toMinutes();
		System.out.println("Quantidade de minutos: "+minutos);
	}

	private static double calculaDoa(int numCommits, int numDevs, boolean primeiroAutor) {
		double primeiraParte = 3.293;
		double segundaParte;
		if (primeiroAutor) {
			segundaParte = 1.098;
		}else {
			segundaParte = 0.0;
		}
		double terceiraParte = 0.164 * numCommits;
		double quartaParte = 0.321 * Math.log(1+numDevs);

		return (primeiraParte + segundaParte + terceiraParte - quartaParte);
	}

	private static void emailsArquivo(List<Revision> commits) {
		for (Revision commit : commits) {
			for (OperationFile arquivo : commit.getFiles()) {
				if(arquivo.getPath().equals("src/Symfony/Bridge/PhpUnit/Legacy/CommandForV6.php")) {
					System.out.println(commit.getAuthor().getEmail());
					System.out.println(commit.getAuthor().getName());
					System.out.println(commit.getDate());
					System.out.println(commit.getExternalId());
					System.out.println();
				}
			}
		}
	}

	private static List<Revision> getRevisions(List<String> arquivos, Git git, Repository repository) throws NoHeadException, GitAPIException, AmbiguousObjectException, IncorrectObjectTypeException, IOException{

		Iterable<RevCommit> log = git.log().call();
		List<Revision> revisions = new ArrayList<Revision>();
		boolean analisa;
		for (RevCommit jgitCommit: log) { //analisa cada commit
			String nome = null, email = null;
			if (jgitCommit.getAuthorIdent() != null) {
				if (jgitCommit.getAuthorIdent().getEmailAddress() != null) {
					email = jgitCommit.getAuthorIdent().getEmailAddress();
				}else {
					email = jgitCommit.getCommitterIdent().getEmailAddress();
				}
				if (jgitCommit.getAuthorIdent().getName() != null) {
					nome = jgitCommit.getAuthorIdent().getName();
				}else {
					nome = jgitCommit.getCommitterIdent().getName();
				}
			}else {
				email = jgitCommit.getCommitterIdent().getEmailAddress();
				nome = jgitCommit.getCommitterIdent().getName();
			}
			List<DiffEntry> diffsForTheCommit = diffsForTheCommit(repository, jgitCommit); //obtém as diffs do commit
			analisa = false;
			for (DiffEntry diff : diffsForTheCommit) { //verifica se o commit modificou um dos arquivos em análise
				String novo = diff.getNewPath();
				String velho = diff.getOldPath();
				if (arquivos.contains(velho) || arquivos.contains(novo)) {
					analisa = true;
				}
			}
			if (analisa) {
				Author author = new Author(nome, email);
				Revision revision = new Revision();
				revision.setExternalId(jgitCommit.getName());
				revision.setAuthor(author);
				revision.setDate(jgitCommit.getAuthorIdent().getWhen());
				revision.setFiles(new ArrayList<OperationFile>());
				revision.setExtracted(true);
				for (DiffEntry diff : diffsForTheCommit) {
					String novo = diff.getNewPath();
					String velho = diff.getOldPath();
					if (arquivos.contains(velho) || arquivos.contains(novo)) {

						OperationFile file = new OperationFile();
						if(diff.getChangeType().name().equals(Constants.ADD)){
							file.setOperationType(OperationType.ADD);
							file.setPath(novo);
						}else if(diff.getChangeType().name().equals(Constants.DELETE)){
							file.setOperationType(OperationType.DEL);
							file.setPath(velho);
						}else if(diff.getChangeType().name().equals(Constants.MODIFY)){
							file.setOperationType(OperationType.MOD);
							file.setPath(novo);
						}else if(diff.getChangeType().name().equals(Constants.RENAME)) {
							file.setOperationType(OperationType.REN);
							file.setPath(novo);
						}else{
							continue;
						}

						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						DiffFormatter diffFormatter = new DiffFormatter( stream );
						diffFormatter.setRepository(repository);
						diffFormatter.format(diff);

						String in = stream.toString();

						Map<String, Integer> modifications = analyze(in);
						file.setLineAdd(modifications.get("adds"));
						file.setLineMod(modifications.get("mods"));
						file.setLineDel(modifications.get("dels"));
						file.setLineCondition(modifications.get("conditions"));
						file.setLinesNumber(file.getLineAdd()+file.getLineDel()+file.getLineMod());
						file.setExtracted(true);
						revision.getFiles().add(file);

						diffFormatter.flush();
						diffFormatter.close();
					}
				}
				revision.setTotalFiles(revision.getFiles().size());
				revisions.add(revision);
			}
		}
		return revisions;
	}

	private static List<DiffEntry> diffsForTheCommit(Repository repo, RevCommit commit) throws IOException, AmbiguousObjectException, 
	IncorrectObjectTypeException { 
		AnyObjectId currentCommit = repo.resolve(commit.getName()); 
		AnyObjectId parentCommit = commit.getParentCount() > 0 ? repo.resolve(commit.getParent(0).getName()) : null; 
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE); 
		df.setBinaryFileThreshold(2 * 1024); //2 MB MAX A FILE
		df.setRepository(repo); 
		df.setDiffComparator(RawTextComparator.DEFAULT); 
		df.setDetectRenames(true); 
		List<DiffEntry> diffs = null; 
		if (parentCommit == null) { 
			RevWalk rw = new RevWalk(repo); 
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree())); 
			rw.close(); 
		} else { 
			diffs = df.scan(parentCommit, currentCommit); 
		} 
		df.close();
		return diffs; 
	}

	private static Map<String, Integer> analyze(String fileDiff){
		Stack<String> additions = new Stack<String>();
		Stack<String> deletions = new Stack<String>();
		int adds = 0, mods = 0, dels = 0, conditions = 0;
		HashMap<String, Integer> modifications = new HashMap<String, Integer>();
		if(fileDiff !=null ){
			String[] lines = fileDiff.split("\n");

			for(int i = 0; i < lines.length; i++){
				if((i > 3) && (lines[i].length() > 0)){
					if((lines[i].charAt(0) == '+') && (lines[i].substring(1).trim().length() > 0)) {
						additions.push(lines[i].substring(1));
					}else if((lines[i].charAt(0) == '-') && (lines[i].substring(1).trim().length() > 0)) {
						deletions.push(lines[i].substring(1));
					}else if ((!additions.isEmpty()) || (!deletions.isEmpty())) {
						for (String temp : additions) {
							if (temp.trim().startsWith("if")) {
								conditions++;
							}
						}
						while((!additions.isEmpty()) || (!deletions.isEmpty())){
							if(additions.isEmpty()){
								deletions.pop();
								dels++;
							} else if(deletions.isEmpty()){
								additions.pop();
								adds++;
							} else {
								String add = additions.pop();
								String del = deletions.pop();
								if(isSimilar(add, del)){
									mods++;
								} else if(additions.size() > deletions.size()){
									deletions.push(del);
									adds++;
								} else {
									additions.push(add);
									dels++;
								}
							}
						}
					}
				}
			}
		}
		modifications.put("adds", adds);
		modifications.put("mods", mods);
		modifications.put("dels", dels);
		modifications.put("conditions", conditions);
		return modifications;
	}

	private static boolean isSimilar(String string1, String string2){
		int result = Levenshtein.getLevenshteinDistance(string1, string2);
		if(((double)result/string1.length()) < 0.4)
			return true;
		return false;

	}

	private static List<String> emails(String email, List<Revision> commits, String nome) {
		List<String> emails = new ArrayList<String>();
		for (Revision revision : commits) {
			if (revision.getAuthor().getEmail().equals(email) == false) {
				String revisionNome = revision.getAuthor().getName();
				if(revisionNome != null) {
					int distancia = StringUtils.getLevenshteinDistance(nome, revisionNome);
					if (revision.getAuthor().getName().equals(nome) || 
							(distancia/(double)nome.length() < 0.3)) {
						emails.add(revision.getAuthor().getEmail());
					}
				}
			}
		}
		return emails;
	}

	private static void projetoAnalisado(List<ModeloOtavio> lista, List<String> projetoArquivos) {
		try{
			XSSFWorkbook workbook = new XSSFWorkbook(new File(caminhoRespostas));
			XSSFSheet sheetDesenvolvedores = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheetDesenvolvedores.iterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				String email = null, nome = null, arquivo = null;
				Date data = null;
				int familiaridade = -1;
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getColumnIndex() == 0) {
						email = cell.getStringCellValue();
					}
					if (cell.getColumnIndex() == 1) {
						nome = cell.getStringCellValue();
					}
					if (cell.getColumnIndex() == 2) {
						arquivo = cell.getStringCellValue();
					}
					if (cell.getColumnIndex() == 3) {
						familiaridade = (int) cell.getNumericCellValue();
					}
					if (cell.getColumnIndex() == 4) {
						data = cell.getDateCellValue();
					}
				}
				if (arquivo != null && projetoArquivos.contains(arquivo) == false) {
					projetoArquivos.add(arquivo);
				}
				if (email != null && nome != null && arquivo != null && data != null) {
					ModeloOtavio modeloOtavio = new ModeloOtavio(email, nome, arquivo, data, familiaridade);
					lista.add(modeloOtavio);
				}
			}
			workbook.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int numCommits(List<Revision> revisions, String email, String filePath, String nome) {
		int numCommits = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						numCommits++;
					}
				}
			}
		}
		return numCommits;
	}

	private static int avgDaysCommits(List<Revision> revisions, String email, String filePath, String nome,
			int numCommits) {
		if (numCommits == 1) {
			return 0;
		}else {
			Date primeiro = null;
			Date ultimo = null;
			List<String> emails = emails(email, revisions, nome);
			for (int i = 0; i < revisions.size(); i++) {
				if (revisions.get(i).getAuthor().getEmail().equals(email) || 
						emails.contains(revisions.get(i).getAuthor().getEmail())) {
					for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
						if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
							if (primeiro == null) {
								primeiro = revisions.get(i).getDate();
							}else if(revisions.get(i).getDate().before(primeiro)) {
								primeiro = revisions.get(i).getDate();
							}
						}
					}
				}
			}
			for (int i = 0; i < revisions.size(); i++) {
				if (revisions.get(i).getAuthor().getEmail().equals(email) || 
						emails.contains(revisions.get(i).getAuthor().getEmail())) {
					for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
						if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
							if (ultimo == null) {
								ultimo = revisions.get(i).getDate();
							}else if(revisions.get(i).getDate().after(ultimo)) {
								ultimo = revisions.get(i).getDate();
							}
						}
					}
				}
			}
			if (primeiro != null && ultimo != null) {
				long diffInMillies = Math.abs(ultimo.getTime() - primeiro.getTime());
				long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
				return (int) (diff/(numCommits - 1));
			}else {
				return -1;
			}
		}
	}

	private static Date lastModify(List<Revision> revisions, String email, String filePath, String nome) {
		Date data = null;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						if (data == null) {
							data = revisions.get(i).getDate();
						}else if(data.before(revisions.get(i).getDate())) {
							data = revisions.get(i).getDate();
						}
					}
				}
			}
		}
		return data;
	}

	private static int somaAdd(List<Revision> revisions, String email, String filePath, String nome) {
		int somaAdd = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						somaAdd = somaAdd + revisions.get(i).getFiles().get(j).getLineAdd();
					}
				}
			}
		}
		return somaAdd;
	}

	private static BlameTotal blameTotal(String nome, String filePath, Repository repository) throws GitAPIException {
		int blame = 0, total = 0;
		BlameCommand blameCommand = new BlameCommand(repository);
		blameCommand.setFilePath(filePath);
		BlameResult blameResult = blameCommand.call();
		if(blameResult == null) {
			System.out.println();
		}
		RawText rawText = blameResult.getResultContents();
		int length = rawText.size();
		for (int i = 0; i < length; i++) {
			PersonIdent autor = blameResult.getSourceAuthor(i);
			if (autor.getName().equals(nome)) {
				blame++;
			}
			total++;
		}
		Extractor extractorOtavio = new Extractor();
		BlameTotal blameTotal = extractorOtavio.new BlameTotal(blame, total);
		return blameTotal;
	}

	private static int somaMontante(List<Revision> revisions, String email, String filePath, String nome) {
		int somaMontante = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						somaMontante = somaMontante + revisions.get(i).getFiles().get(j).getLinesNumber();
					}
				}
			}
		}
		return somaMontante;
	}

	private static boolean primeiroAutor(List<Revision> revisions, String email, String filePath, String nome) {
		boolean primeiroAutor = false;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath) &&
							revisions.get(i).getFiles().get(j).getOperationType().equals(OperationType.ADD)) {
						primeiroAutor = true;
					}
				}
			}
		}
		return primeiroAutor;
	}

	private static int somaDel(List<Revision> revisions, String email, String filePath, String nome) {
		int somaDel = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						somaDel = somaDel + revisions.get(i).getFiles().get(j).getLineDel();
					}
				}
			}
		}
		return somaDel;
	}

	private static int somaMod(List<Revision> revisions, String email, String filePath, String nome) {
		int somaMod = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						somaMod = somaMod + revisions.get(i).getFiles().get(j).getLineMod();
					}
				}
			}
		}
		return somaMod;
	}

	private static int somaCond(List<Revision> revisions, String email, String filePath, String nome) {
		int somaCond = 0;
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
						somaCond = somaCond + revisions.get(i).getFiles().get(j).getLineCondition();
					}
				}
			}
		}
		return somaCond;
	}

	private static int numeroOtherFile(List<Revision> revisions, String email, String nome) {
		List<String> arquivos = new ArrayList<String>();
		List<String> emails = emails(email, revisions, nome);
		for (int i = 0; i < revisions.size(); i++) {
			if (revisions.get(i).getAuthor().getEmail().equals(email) || 
					emails.contains(revisions.get(i).getAuthor().getEmail())) {
				for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
					if (arquivos.contains(revisions.get(i).getFiles().get(j).getPath()) == false) {
						arquivos.add(revisions.get(i).getFiles().get(j).getPath());
					}
				}
			}
		}
		return arquivos.size();
	}

	private static int numberModsDevelopers(List<Revision> revisions, String email, String filePath, Date last, String nome) {
		int numMod = 0;
		List<String> emails = emails(email, revisions, nome);
		if(last != null) {
			for (int i = 0; i < revisions.size(); i++) {
				if (revisions.get(i).getDate().after(last) && 
						!revisions.get(i).getAuthor().getEmail().equals(email) && 
						emails.contains(revisions.get(i).getAuthor().getEmail()) == false) {
					for (int j = 0; j < revisions.get(i).getFiles().size(); j++) {
						if (revisions.get(i).getFiles().get(j).getPath().equals(filePath)) {
							numMod++;
						}
					}
				}
			}
		}else {
			System.out.println("Problema: "+email+", "+filePath);
		}
		return numMod;
	}

	class BlameTotal{
		private int blame;
		private int total;

		public BlameTotal(int blame, int total) {
			this.blame = blame;
			this.total = total;
		}
	}
}
