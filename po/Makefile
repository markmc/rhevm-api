
ALL_LINGUAS = en_GB

ALL_PROPERTIES = $(foreach LANG, $(ALL_LINGUAS), $(LANG).properties)

all: $(ALL_PROPERTIES)

rhevm-api.pot:
	prop2po -P Messages.properties rhevm-api.pot

en_GB.po: rhevm-api.pot
	perl en_GB.pl rhevm-api.pot > en_GB.po

%.properties: %.po
	po2prop -t Messages.properties $< $@

clean:
	rm -f $(ALL_PROPERTIES) *~
