
all: html html-single pdf

html:
	publican build --langs=en-US --formats=html

html-single:
	publican build --langs=en-US --formats=html-single

pdf:
	publican build --langs=en-US --formats=pdf

clean:
	publican clean

upload:
	rsync -e ssh -vaPz --del --exclude=xml* tmp/en-US fedorapeople.org:./public_html/rhevm-api/
