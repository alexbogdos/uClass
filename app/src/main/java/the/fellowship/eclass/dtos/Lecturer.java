package the.fellowship.eclass.dtos;

public class Lecturer {
    private final String name;
    private String contactDetails;
    private String visitingHours;

    public Lecturer(String name) {
        this.name = translate(name.strip());
    }

    public String getName() {
        return name;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }

    public String getVisitingHours() {
        return visitingHours;
    }

    public void setVisitingHours(String visitingHours) {
        this.visitingHours = visitingHours;
    }

    /**
     * Translate the lecturer's name from <a href="https://eclass.aueb.gr/">eclass.aueb.gr</a>
     * to the one assigned in <a href="https://aueb.gr/">aueb.gr</a>
     * <p>
     * WARNING: This only works for lecturers found in <a href="https://www.dept.aueb.gr/el/content/CS_OfficeHours">dept.aueb.gr/el/content/CS_OfficeHours</a>
     *
     * @param name
     * @return
     */
    private static String translate(String name) {
        switch (name) {
            case "Athanassios Androutsos":
            case "ΑΘΑΝΑΣΙΟΣ ΑΝΔΡΟΥΤΣΟΣ":
            case "Αθανάσιος Ανδρούτσος":
                return "Ανδρουτσόπουλος Ιωάννης";
            case "Eugenie Foustoucos":
            case "ΦΟΥΣΤΟΥΚΟΥ ΕΥΓΕΝΙΑ":
            case "ΕΥΓΕΝΙΑ ΦΟΥΣΤΟΥΚΟΥ":
                return "Φουστούκου Ευγενία";
            case "Evangelos Markakis":
            case "EVANGELOS MARKAKIS":
            case "MARKAKIS EVANGELOS":
                return "Μαρκάκης Ευάγγελος";
            case "Ioannis Kontogiannis":
            case "IOANNIS PAVLOPOULOS":
                return "Παυλόπουλος Ιωάννης";
            case "Katerinis Panagiotis":
            case "ΠΑΝΑΓΙΩΤΗΣ  ΚΑΤΕΡΙΝΗΣ":
            case "ΠΑΝΑΓΙΩΤΗΣ ΚΑΤΕΡΙΝΗΣ":
                return "Κατερίνης Παναγιώτης";
            case "DIMAKIS ANTONIS":
            case "Αντώνης Δημάκης":
                return "Δημάκης Αντώνιος";
            case "Malevris Nicos":
                return "Μαλεύρης Νικόλαος";
            case "MARIAS IOANNIS":
                return "Μαριάς Ιωάννης";
            case "PANAGIOTIS KOTSIOS":
                return "Κώτσιος Παναγιώτης";
            case "Spyros Voulgaris":
            case "Σπύρος Βούλγαρης":
                return "Βούλγαρης Σπυρίδων";
            case "STAVROS TOUMPIS":
            case "ΣΤΑΥΡΟΣ ΤΟΥΜΠΗΣ":
            case "Σταύρος Τουμπής":
                return "Τουμπής Σταύρος";
            case "EVANGELIA VAGENA - ΕΥΑΓΓΕΛΙΑ ΒΑΓΕΝΑ":
                return "Βαγενά Ευαγγελία";
            case "Vassalos, Vasilis (Βασσάλος Βασίλης)":
            case "Βασσάλος Βασίλης/Vassalos Vasilis":
            case "ΒΑΣΙΛΗΣ ΒΑΣΣΑΛΟΣ":
                return "Βασσάλος Παρασκευάς";
            case "Xylomenos George":
            case "Γεώργιος Ξυλωμένος":
                return "Ξυλωμένος Γεώργιος";
            case "ΑΛΚΜΗΝΗ ΣΓΟΥΡΙΤΣΑ":
                return "Σγουρίτσα Αλκμήνη";
            case "Ανδρέας Α. Βασιλάκης":
                return "Βασιλάκης Ανδρέας - Αλέξανδρος";
            case "Βασίλειος Σύρης":
            case "ΣΥΡΗΣ ΒΑΣΙΛΕΙΟΣ / SIRIS VASILIOS":
                return "Σύρης Βασίλειος";
            case "ΒΑΣΙΛΙΚΗ ΚΑΛΟΓΕΡΑΚΗ":
                return "Καλογεράκη Βασιλική";
            case "Βασιλική Μπρίνια":
                return "Μπρίνια Βασιλική";
            case "ΓΕΩΡΓΙΟΣ ΑΜΑΝΑΤΙΔΗΣ":
                return "Αμανατίδης Γεώργιος";
            case "Γεώργιος Δ. Σταμούλης":
            case "Γεώργιος Σταμούλης":
                return "Σταμούλης Γεώργιος";
            case "ΓΕΩΡΓΙΟΣ Κ. ΠΟΛΥΖΟΣ":
            case "Γεώργιος Κ. Πολύζος":
                return "Πολύζος Γεώργιος";
            case "ΓΕΩΡΓΙΟΣ ΠΑΠΑΪΩΑΝΝΟΥ (Georgios Papaioannou)":
                return "Παπαϊωάννου Γεώργιος";
            case "Γιάννης Κωτίδης":
                return "Κωτίδης Ιωάννης";
            case "ΘΕΟΦΙΛΟΣ ΜΑΪΛΗΣ  - Theofilos Mailis":
                return "Μαΐλης Θεόφιλος";
            case "ΙΟΡΔΑΝΗΣ ΚΟΥΤΣΟΠΟΥΛΟΣ":
            case "Ιορδάνης Κουτσόπουλος":
                return "Κουτσόπουλος Ιορδάνης";
            case "Άννα Κεφάλα":
                return "Κεφάλα Άννα";
            case "Καθ. Δημήτρης Α. Γκρίτζαλης":
                return "Γκρίτζαλης Δημήτριος";
            case "Πάνος Κωνσταντόπουλος":
                return "Κωνσταντόπουλος Πάνος";
            case "Ρέμος Αρμάος":
                return "Αρμάος Ρέμος";
            default:
                return name;
        }
    }
}
