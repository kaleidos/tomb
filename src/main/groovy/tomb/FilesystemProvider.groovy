package tomb

import java.nio.file.Path

interface FilesystemProvider {

    InputStream get(Path path)

    void put(InputStream inputStream, Path path)

    List<String> list(Path path)

    void copy(Path initialPath, Path destinationPath)

    void move(Path initialPath, Path destinationPath)

    void delete(Path path)

    URI getUri(Path path)

}
