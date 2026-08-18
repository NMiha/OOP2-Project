package persistence;

import java.io.File;

import model.Dataset;

public interface Storage {

	void save(Dataset data, File file) throws StorageException;
	Dataset load(File file) throws StorageException;
	String formatName();
	String extension();
}
