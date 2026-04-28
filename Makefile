BUILD_DIR = build

SRC_DIR = src
SRC_MAIN = $(SRC_DIR)/Main.java

ENTRY_CLASS = Main

build:
	javac -d $(BUILD_DIR) -sourcepath $(SRC_DIR) $(SRC_MAIN)

run:
	java -cp $(BUILD_DIR) $(ENTRY_CLASS)

clean:
	rm -rf $(BUILD_DIR)

.PHONY: build run clean
