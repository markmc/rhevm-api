#!/usr/bin/perl

# (c) 2000 Abigail Brady
#     2002-2006 Bastien Nocera
#     2005 James A. Morrison

# Released under the GNU General Public Licence, either version 2
# or at your option, any later version

# NO WARRANTY!

# Script to take a .pot file and make an en_GB translation of it.
# since full AI hasn't been invented yet, you need to inspect it
# by hand afterwards.
#
# Interactive mode added for certain words by Peter Oliver, 2002-08-25.

# These are as-is

# en_AI         Anguillan English
# en_AC         Ascension Island English
# en_AU         Australian English
# en_AQ         Antartican English
# en_BB         Barbados English
# en_BM         Bermudan English
# en_BS         Bahamas English
# en_BZ         Belize English
# en_CX         Christmas Island English
# en_EU         European English
# en_FK         Fakland Islands English
# en_GB         British English
# en_GI         Gibraltan English
# en_GG         Guernsey English
# en_HK         Hong Kong English
# en_IE         Irish English
# en_IM         Manx English
# en_IO         British Indian Ocean English
# en_HM         Heard & McDonald Islands English
# en_JE         Jersey English
# en_JM         Jamaican English
# en_KY         Cayman Islands English
# en_MS         Montserrat English
# en_NF         Norfolk Island English
# en_NZ         New Zealand English
# en_PN         Pitcairn English
# en_SH         Saint Helena English
# en_TC         Turks & Caicos English
# en_VG         British Virgin Isles English
# en_ZA         South African English

# en_CA         Canadian English
#               (Canadian uses some US forms)

# The government websites of the following countries use British forms.

# en_BW         Botswanan English
# en_KE         Kenyan English
# en_ZM         Zambian English
# en_ZW         Zimbabwe English



# if you find an inaccuracy in this list, please mail me at
# <rwb197@zepler.org>

use strict;
use warnings;
use Time::gmtime;
use Term::ReadLine;
use vars qw($msg_str $msg_id $locale $rl);

sub do_trans {
  my ($tf, $tt) = @_;
  my ($accel, $upper_tt );

# Handle keyboard shortcuts in message strings.  Messy, but it seems to work.
# Use the same key if possible, otherwise prompt the user.
  my $underscores_tf = $tf;
  $underscores_tf =~ s/(?<=\w)(.)/_?$1/g;
  if ( $msg_str =~ m/(_?$underscores_tf)/i and $1 =~ m/_(\w)/ ) {
      my $accel = $1;
      if ( $tt =~ s/(?=$accel)/_/i ) {
          if ( $tt =~ m/^_$accel/i ) {
              $upper_tt = $tt;
              $upper_tt =~ s/^_(.)/_\u$1/;
          }
      }
      else {
          my $nonl = $msg_str;
          chomp $nonl;
          print STDERR "\nI want to change '$tf' to '$tt' in the message $nonl, but don't know where to put the accelerator.\n";
          $rl->addhistory( $nonl );
          $msg_str = $rl->readline( 'New msg_str? ' ). "\n";
          print STDERR "Replaced.\n";
          return;
      }
  }

  $msg_str =~ s/(\b|_)\u$underscores_tf/$upper_tt/g and return
      if defined $upper_tt;
  $msg_str =~ s/(\b|_)$underscores_tf/$tt/g;
  $msg_str =~ s/(\b|_)\u$underscores_tf/\u$tt/g;
  $msg_str =~ s/(\b|_)\U$underscores_tf/\U$tt/g;
}

sub query_trans {
    return; # markmc hack
    my ($tf, $tt, $context) = @_;
    if ( $msg_str =~ m/\b$tf/i ) {
           my $result;

        print STDERR "\nmsgid: ${msg_id}msgstr: $msg_str";
        if ($context) {
            $result = $rl->readline( "Change '$tf' to '$tt'? (Context: '$context') (y/N) " );
        } else {
            $result = $rl->readline( "Change '$tf' to '$tt'? (y/N) " );
        }
        if ( $result =~ m/^\s*y(es)?\s*$/i ) {
            print STDERR "Changed\n";
            do_trans( $tf, $tt );
        }
        else {
            print STDERR "Not changed\n";
        }
    }
}

sub translate() {
  if (!($msg_str eq "\"\"\n")) {

    my $date = sprintf("%04i-%02i-%02i %02i:%02i+0000", gmtime()->year+1900,
    gmtime()->mon+1, gmtime()->mday, gmtime()->hour, gmtime()->min);

    $msg_str =~ s/YEAR-MO-DA HO:MI\+ZONE/$date/;
    $msg_str =~ s/YEAR-MO-DA HO:MI\+DIST/$date/;
    $msg_str =~ s/FULL NAME <EMAIL\@ADDRESS>/Abigail Brady <morwen\@evilmagic.org>/;
    $msg_str =~ s/CHARSET/UTF-8/;
    $msg_str =~ s/ENCODING/8-bit/;
    $msg_str =~ s/LANGUAGE <LL\@li.org>//;
    $msg_str =~ s/Plural-Forms: nplurals=INTEGER; plural=EXPRESSION/Plural-Forms: nplurals=2; plural=n != 1;/;
    return;
  }

  # Epiphany-style contexting
  # FIXME we should save the context and pass it
  if ( $msg_id =~ m/^.*\|(.*)$/ ) {
    $msg_str = "\"".$1."\n";
  } else {
    $msg_str = $msg_id;
  }

  do_trans("aluminum", "aluminium");
  do_trans("analog", "analogue");
  do_trans("analyze", "analyse");
  do_trans("analyzer", "analyser");
  do_trans("armor", "armour");
  do_trans("authorization", "authorisation");
  do_trans("authorize", "authorise");
  do_trans("authorized", "authorised");
  do_trans("behavior","behaviour");
  do_trans("caliber", "calibre");
  do_trans("cancelation", "cancellation");
  do_trans("canceled", "cancelled");
  do_trans("canceling", "cancelling");
  do_trans("catalog", "catalogue");
  query_trans("cell", "mobile", "Refers to phones");
  do_trans("centimeter", "centimetre");
  do_trans("centered", "centred");
  do_trans("center", "centre");
  # These are less common, and the below translations are more useful, see bgo#628507.
  #query_trans("checked", "chequered", "Refers to patterns");
  #query_trans("check", "cheque", "Refers to a payment method");
  query_trans("checked", "ticked", "Refers to an UI element");
  query_trans("check", "tick", "Refers to an UI element");
  do_trans("cipher", "cypher");
  do_trans("color", "colour");
  do_trans("colorize", "colour");
  do_trans("colorized", "coloured");
  do_trans("customization", "customisation");
  do_trans("daemonize", "daemonise");
  do_trans("defense", "defence");
  do_trans("deserialize", "deserialise");
  do_trans("dialer", "dialler");
  do_trans("dialing", "dialling");
  do_trans("dialed", "dialled");
  do_trans("dialog", "dialogue");
  do_trans("digitized", "digitised");
  do_trans("eggplant","aubergine");
  do_trans("email", "e-mail");
  do_trans("encyclopedia", "encyclopaedia");
  do_trans("endeavor", "endeavour");
  do_trans("equaled", "equalled");
  do_trans("equaling", "equalling");
  do_trans("favor", "favour");
  do_trans("fiber", "fibre");
  do_trans("flavor", "flavour");
  do_trans("fueled", "fuelled");
  do_trans("fueling", "fuelling");
  query_trans("harbor", "harbour");
  do_trans("gray", "grey");
  do_trans("grayscale", "greyscale");
  do_trans("harbor", "harbour");
  do_trans("honor", "honour");
  do_trans("humor", "humour");
  do_trans("internationalization", "internationalisation");
  do_trans("itemize", "itemise");
  do_trans("itemized", "itemised");
  do_trans("jeweled", "jewelled");
  do_trans("judgment", "judgement");
  do_trans("kilometer", "kilometre");
  do_trans("labeled", "labelled");
  query_trans("license", "licence");
  do_trans("labor", "labour");
  do_trans("liter", "litre");
  do_trans("litreal", "literal"); # Because "liter" sometimes affects other words (FIXME: bug #574576)
  do_trans("litreate", "literate");
  do_trans("maximization", "maximisation");
  do_trans("maximize", "maximise");
  do_trans("maximized", "maximised");
  query_trans("meter", "metre");
  do_trans("millimeter", "millimetre");
  do_trans("minimize", "minimise");
  do_trans("minimized", "minimised");
  do_trans("misspelled", "misspelt");
  do_trans("modeled", "modelled");
  do_trans("modeler", "modeller");
  do_trans("modeling", "modelling");
  do_trans("neighbor", "neighbour");
  do_trans("offense", "offence");
  do_trans("optimized", "optimised");
  do_trans("optimizing", "optimising");
  do_trans("organization", "organisation");
  do_trans("organizational", "organisational");
  do_trans("paneled", "panelled");
  do_trans("paneling", "panelling");
  do_trans("personalize", "personalise");
  do_trans("personalizing", "personalising");
  do_trans("popularized", "popularised");
  query_trans("practise","practice");
  do_trans("prioritize", "prioritise");
  do_trans("randomize", "randomise");
  do_trans("recognize", "recognise");
  do_trans("rumor", "rumour");
  do_trans("saber", "sabre");
  do_trans("scepter", "sceptre");
  do_trans("serialized", "serialised");
  do_trans("signaled", "signalled");
  do_trans("signaling", "signalling");
  do_trans("simultanous", "simultaneous");
  do_trans("specter", "spectre");
  do_trans("spelled", "spelt");
  do_trans("stickyness", "stickiness");
  do_trans("sulfur", "sulphur");
  do_trans("summarize", "summarise");
  do_trans("synchronization", "synchronisation");
  do_trans("synchronize", "synchronise");
  do_trans("synchronized", "synchronised");
  do_trans("synchronizes", "synchronises");
  do_trans("synchronizing", "synchronising");
  do_trans("theater", "theatre");
  query_trans("tire", "tyre");
  do_trans("totaled", "totalled");
  do_trans("totaler", "totaller");
  do_trans("totaling", "totalling");
  do_trans("tunneling", "tunnelling");
  do_trans("unauthorized", "unauthorised");
  do_trans("uncategorized", "uncategorised");
  do_trans("unmaximize", "unmaximise");
  do_trans("unminimize", "unminimise");
  do_trans("unminimized", "unminimised");
  do_trans("unminimizing", "unminimising");
  do_trans("unrecognized", "unrecognised");
  do_trans("utilization", "utilisation");
  do_trans("utilize", "utilise");
  do_trans("utilized", "utilised");
  do_trans("utilizing", "utilising");
  do_trans("vapor", "vapour");
  do_trans("vise", "vice");
  do_trans("visualization", "visualisation");

   if ($locale eq "en_CA") {
     do_trans("anti-clockwise", "counterclockwise");
     do_trans("categorise","categorize");
     do_trans("customise", "customize");
     do_trans("customised", "customized");
     do_trans("initialisation", "initialization");
     do_trans("initialise", "initialize");
     do_trans("initialising", "initializing");
     do_trans("initialized", "initialized");
     do_trans("organise", "organize");
     do_trans("routeing", "routing");
     do_trans("trash", "garbage");
     do_trans("uninitialised","uninitialized");
     do_trans("wastebasket", "garbage");
     do_trans("translator_credits", "Abigail Brady <morwen\@evilmagic.org>\\n\"\n\"Bastien Nocera <hadess\@hadess.net>\\n\"\n\"James A. Morrison <jim.morrison\@gmail.com>");
   } else {
     query_trans("canfield", "demon", "Refers to a solitaire card game variant, see http://bugzilla.gnome.org/show_bug.cgi?id=444409");
     do_trans("categorize","categorise");
     do_trans("counterclockwise", "anti-clockwise");
     do_trans("customize", "customise");
     do_trans("customized", "customised");
     query_trans("Genesis", "Megadrive", "Refers to the Sega gaming console");
     do_trans("initialization", "initialisation");
     do_trans("initialize", "initialise");
     do_trans("initializing", "initialising");
     do_trans("initialized", "initialised");
     query_trans("klondike", "canfield", "Refers to a solitaire card game variant, see http://bugzilla.gnome.org/show_bug.cgi?id=444409");
     query_trans("NES", "Nintendo", "Refers to the SuperNES console, only the Super one!");
     do_trans("organize", "organise");
     do_trans("rerouting", "rerouteing");
     do_trans("routing", "routeing");
     do_trans("uninitialized","uninitialised");
     do_trans("_trash", "wastebaske_t");
     do_trans("trash", "wastebasket");
     do_trans("translator_credits", "Abigail Brady <morwen\@evilmagic.org>\\n\"\n\"Bastien Nocera <hadess\@hadess.net>\\n\"\n\"Philip Withnall <philip\@tecnocode.co.uk>");
   }

# This causes the string not to be copied
#  if ($msg_str eq $msg_id) {
#    $msg_str = "\"\"\n";
#  }
}

# Modes:
# 1 = adding to msgid
# 2 = adding to msgstr
# 3 = adding to plural msgid
# 4 = adding to plural msgstr
my $mode = 0;

$rl = Term::ReadLine->new("String Replacement");
$locale = $ENV{'LANG'};
$locale =~ s/\..*//g;

my $msg_id2 = "";
my $msg_str2 = "";
$msg_str = "";

while (<>) {
   if  (/^#/) {
     s/SOME DESCRIPTIVE TITLE/English (British) translation/;
     my $year = gmtime()->year+1900;
     s/YEAR/$year/;
     s/FIRST AUTHOR <EMAIL\@ADDRESS>/Abigail Brady <morwen\@evilmagic.org>, Bastien Nocera <hadess\@hadess.net>/;
     print unless ((/^#, fuzzy/) && ($mode eq 0));
   } elsif (/^msgid_plural /) {
     $msg_id2 .= substr($_, 13);
     $mode = 3;
   } elsif (/^msgstr\[0\]/) {
     $msg_str .= substr($_, 10);
     $mode = 2;
   } elsif (/^msgstr\[1\]/) {
     $msg_str2 .= substr($_, 10);
     $mode = 4;
   } elsif (/^msgid /) {
     $msg_id .= substr($_, 6);
     $mode = 1;
   } elsif (/^msgstr "/) {
     $msg_str .= substr($_, 7);
     $mode = 2;
   } elsif (/^"/) {
       if ($mode == 1) {
         $msg_id .= $_;
         $mode = 1;
       } elsif ($mode == 3) {
               $msg_id2 .= $_;
               $mode = 3;
       } elsif ($mode == 2) {
         $msg_str .= $_;
         $mode = 2;
       } elsif ($mode == 4) {
               $msg_str2 .= $_;
               $mode = 4;
       }
   } else {
     my $line = $_;

     if (defined $msg_id2 && $msg_id2 ne "") {
       translate();
       print "msgid $msg_id";
       print "msgid_plural $msg_id2";
       print "msgstr[0] $msg_str";

       $msg_id = $msg_id2;
       $msg_str = $msg_str2;
       translate();
       print "msgstr[1] $msg_str";
       $msg_id = "";
       $msg_id2 = "";
       $msg_str = "";
       $msg_str2 = "";
     } elsif ($msg_id || $msg_str) {
       translate();
       print "msgid $msg_id";
       print "msgstr $msg_str";
       $msg_id = "";
       $msg_id2 = "";
       $msg_str = "";
       $msg_str2 = "";
     }
     print $line;
   }

}


if ($msg_id || $msg_str) {
  translate();
  print "msgid $msg_id";
  print "msgstr $msg_str";
  $msg_id = "";
  $msg_str = "";
}
