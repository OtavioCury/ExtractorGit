package modelo;

import java.util.Date;

public class ModeloOtavio {
	
	private String email;
	private String nome;
	private String arquivo;
	private int familiaridade;
	private Date data;
	/**
	 * @param email
	 * @param nome
	 * @param arquivo
	 */
	public ModeloOtavio(String email, String nome, String arquivo, Date data, int familiaridade) {
		super();
		this.email = email;
		this.nome = nome;
		this.arquivo = arquivo;
		this.setData(data);
		this.familiaridade = familiaridade;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getArquivo() {
		return arquivo;
	}
	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public int getFamiliaridade() {
		return familiaridade;
	}
	public void setFamiliaridade(int familiaridade) {
		this.familiaridade = familiaridade;
	}
	
}
