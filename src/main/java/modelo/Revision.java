/**
 * 
 */
package modelo;

import java.util.Date;
import java.util.List;

/**
 * @author Werney Ayala
 *
 */
public class Revision{

	private String externalId;
	
	private Author author;
	
	private Date date;
	
	private int totalFiles;
	
	private boolean extracted;
	
	private List<OperationFile> files;
	
	public Revision() { 
	}
	
	/**
	 * @return the author
	 */
	public Author getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(Author author) {
		this.author = author;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the files
	 */
	public List<OperationFile> getFiles() {
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List<OperationFile> files) {
		this.files = files;
	}

	/**
	 * @return the totalFiles
	 */
	public int getTotalFiles() {
		return totalFiles;
	}

	/**
	 * @param totalFiles the totalFiles to set
	 */
	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}

	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * @return the extracted
	 */
	public boolean isExtracted() {
		return extracted;
	}

	/**
	 * @param extracted the extracted to set
	 */
	public void setExtracted(boolean extracted) {
		this.extracted = extracted;
	}

}
