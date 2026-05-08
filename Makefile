BUILD_DIR = build

SRC_DIR = src
SRC_MAIN = $(SRC_DIR)/Main.java

ENTRY_CLASS = Main

build:
	javac -d $(BUILD_DIR) -sourcepath $(SRC_DIR) $(SRC_MAIN)
	ln -sfn $(realpath src/boogle/ui/tui/asset) build/boogle/ui/tui/asset
	ln -sfn $(realpath src/boogle/sound/asset) build/boogle/sound/asset

run-tui:
	java -cp $(BUILD_DIR) $(ENTRY_CLASS) tui

run-gui:
	java -cp $(BUILD_DIR) $(ENTRY_CLASS) gui

clean:
	rm -rf $(BUILD_DIR)

.PHONY: build run clean
