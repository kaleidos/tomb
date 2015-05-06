package tomb

interface FilesystemProvider {

    InputStream get(String path)

    void put(InputStream inputStream, String path)

    List<String> list(String path)

    void copy(String initialPath, String destinationPath)

    void move(String initialPath, String destinationPath)

    void delete(String path)

    URL getUrl(String path)

}
