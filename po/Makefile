
ALL_LINGUAS = en_GB
ALL_PREFIXES = Messages AppErrors VdsmErrors

POFILES = $(foreach LINGUA, $(ALL_LINGUAS), $(LINGUA).po)
INFILES = $(foreach PREFIX, $(ALL_PREFIXES), $(PREFIX).properties)
OUTFILES = $(foreach LANG, $(ALL_LINGUAS), $(foreach PREFIX, $(ALL_PREFIXES), $(PREFIX)_$(LANG).properties))

all: $(OUTFILES)

Merged.properties: $(INFILES)
	cat $^ | sed '/^[ \t]*$$/d' > $@

rhevm-api.pot: Merged.properties
	prop2po -P $< $@

en_GB.po: rhevm-api.pot
	perl en_GB.pl $< > $@

$(OUTFILES): $(INFILES) $(POFILES)
	for pre in $(ALL_PREFIXES); do \
	    for lang in $(ALL_LINGUAS); do \
	        po2prop -t $${pre}.properties $${lang}.po $${pre}_$${lang}.properties; \
	    done; \
	done

clean:
	rm -f Merged.properties $(OUTFILES) *~
