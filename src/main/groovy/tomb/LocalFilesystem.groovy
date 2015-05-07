package tomb

import java.nio.file.Path

class LocalFilesystem implements FilesystemProvider {

    Path basePath

    LocalFilesystem(Path basePath) {
        File basePathFile = basePath.toFile()
        if (!basePathFile.exists()) {
            throw new FilesystemException("Path ${basePath} doesn't exist")
        }

        if (!basePathFile.isDirectory()) {
            throw new FilesystemException("Path ${basePath} is not a directory")
        }

        this.basePath = basePath
    }

    Path resolve(Path path) {
        return basePath.resolve(path)
    }

    InputStream get(Path relativePath) {
        Path path = resolve(relativePath)
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${path} is not a file")
        }

        return file.newInputStream()
    }

    void put(InputStream inputStream, Path path) {
        File file = path.toFile()

        try {
            file.withOutputStream { it.write(inputStream.bytes) }
        } catch (Exception e) {
            throw new FilesystemException("Unable to write file ${path}")
        }
    }

    List<String> list(Path path = Paths.get('')) {
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("Directory ${path} doesn't exist")
        }

        if (!file.isDirectory()) {
            throw new FilesystemException("Path ${path} is not a directory")
        }

        return f.list()
    }

    void copy(Path initialPath, Path destinationPath) {
        File initialFile = initialPath.toFile()

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        File destinationFile = destinationPath.toFile()

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        destinationFile.withOutputStream { it.write(initialFile.bytes) }
    }

    void move(Path initialPath, Path destinationPath) {
        File initialFile = initialPath.toFile()

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        File destinationFile = destinationPath.toFile()

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        destinationFile.withOutputStream { it.write(initialFile.bytes) }
        initialFile.delete()
    }

    void delete(Path path) {
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${path} is not a file")
        }

        file.delete()
    }

    URI getUri(Path path) {
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        return file.toURI()
    }

}
