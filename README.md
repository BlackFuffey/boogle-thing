# Boogle
Boogle is a TUI implementation of the classic Boggle tabletop game in Java.

## Building & Running
Dependencies:
 - JDK Version >=9, with `javac`, `java`, and `jar` available in `$PATH`
 - Unix Make
 - A terminal with 256-bit color and box drawing character support

### Building
```sh
make
```

### Running
```sh
make run-tui    # use TUI interface
make run-gui    # use GUI interface
```

**TIP**: If you are on Windows, you may have to run `chcp 65001` to set terminal locale to UTF-8 first. Otherwise certain text may not display correctly.

### Bundling
To bundle build results as jar, use
```sh
make bundle
```
