package tomb

class LocalFilesystem implements FilesystemProvider {

    InputStream get(String path) {
        File file = new File(path)

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${path} is not a file")
        }

        return file.newInputStream()
    }

    void put(InputStream inputStream, String path) {
        try {
            File file = new File(path)
            file.append(inputStream)
        } catch(Exception e) {
            throw new FilesystemException("Unable to write file ${path}")
        }
    }

    List<String> list(String path) {
        File file = new File(path)

        if (!file.exists()) {
            throw new FilesystemException("Directory ${path} doesn't exist")
        }

        if (!file.isDirectory()) {
            throw new FilesystemException("Path ${path} is not a directory")
        }

        return f.list()
    }

    void copy(String initialPath, String destinationPath) {
        File initialFile = new File(initialPath)

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        File destinationFile = new File(destinationPath)

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        FileInputStream inputStream = initialFile.newInputStream()
        destinationFile.append(inputStream)
    }

    void move(String initialPath, String destinationPath) {
        File initialFile = new File(initialPath)

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        File destinationFile = new File(destinationPath)

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        FileInputStream inputStream = initialFile.newInputStream()
        destinationFile.append(inputStream)
        initialFile.delete()
    }

    void delete(String path) {
        File file = new File(path)

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${path} is not a file")
        }

        file.delete()
    }

    URI getUri(String path) {
        File file = new File(path)

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        return file.toURI()
    }

}
