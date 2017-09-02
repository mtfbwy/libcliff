#-----------------------------------------------------------
# makefile for cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(STD_HEADER) $(HEADERS))

PACKAGE := libcliff

TOP := .
SRC := $(TOP)/src/$(subst .,/,$(PACKAGE))

OUT := $(TOP)/out
OUT_CC := $(OUT)/cc
OUT_TARGET_H := $(OUT)/$(PACKAGE).h
OUT_TARGET_A := $(OUT)/$(PACKAGE).a
OUT_TARGET_O := $(OUT)/$(PACKAGE).o
OUT_TARGET := $(OUT_TARGET_H) $(OUT_TARGET_A) $(OUT_TARGET_O)

STD_HEADER := $(SRC)/std.h

HEADERS := $(SRC)/predefined.h $(SRC)/libcliff.h
OBJECTS :=

#-----------------------------------------------------------
# lib

LOCAL_SRC_DIR := $(SRC)/primitive

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------
# abstract data type

LOCAL_SRC_DIR := $(SRC)/adt

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
#HEADERS += $(addprefix $(LOCAL_SRC_DIR)/,Blob.h KeyValue.h linear/ListHead.h linear/List.h linear/Deque.h HashMap.h)
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------

OBJECTS := $(strip $(OBJECTS))

.PHONY: cc
cc: $(OUT_TARGET)

$(OUT_TARGET_H): $(HEADERS)
	@echo "generating .h ..."
	@mkdir -p $(OUT_CC)
	@cat $(HEADERS) > $@

$(OUT_TARGET_A): $(OBJECTS)
	@echo "archiving ..."
	@ar cr $@ $^

$(OUT_TARGET_O): $(OBJECTS)
	@echo "linking ..."
	@ld -r $^ -o $@

$(OUT_CC)/%.o : $(SRC)/%.c
	@-mkdir -p $(@D)
	@$(CC) $(CCFLAGS) -c $< -o $@

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_TARGET)
	@rm -rf $(OUT_CC)
