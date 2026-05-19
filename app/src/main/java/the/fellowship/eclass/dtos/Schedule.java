package the.fellowship.eclass.dtos;

import java.util.List;

public abstract class Schedule {
    public final static List<Lesson> schedule = List.of(
            // 2nd Semester
            new Lesson("Μαθηματικά ΙΙ",                                                           new Occurrence("ΤΡ", "13-15", "Αμφ.Α"),     new Occurrence("ΠΕ", "11-13", "Αμφ.Α")),
            new Lesson("Σχεδίαση Ψηφιακών Συστημάτων",                                            new Occurrence("ΤΡ", "15-17", "Αμφ.Α"),     new Occurrence("ΠΕ", "13-15", "Αμφ.Α")),
            new Lesson("Πιθανότητες",                                                             new Occurrence("ΤΡ", "9-11",  "Αμφ.Α"),     new Occurrence("ΤΡ", "15-17", "Αμφ.Α"),     new Occurrence("ΠΑ", "9-11", "Αμφ.Α")),
            new Lesson("Προγραμματισμός Υπολογιστών με Java",                                     new Occurrence("ΔΕ", "11-13", "Αμφ.Α"),     new Occurrence("ΤΕ", "15-17", "Αμφ.Α")),
            new Lesson("Εισαγωγή στη Διοίκηση Επιχειρήσεων",                                      new Occurrence("ΤΡ", "19-21", "Αμφ.Α"),     new Occurrence("ΤΕ", "13-15", "Αμφ.Α")),
            
            new Lesson("Μαθηματικά ΙΙ (Φροντιστήριο)",                                            new Occurrence("ΤΡ", "17-19", "Αμφ.Α")),
            new Lesson("Προγραμματισμός Υπολογιστών με Java (Φροντιστήριο)",                      new Occurrence("ΤΡ", "9-11",  "ΗΥ1 & ΗΥ2"), new Occurrence("ΤΡ", "11-13", "ΗΥ1 & ΗΥ2"), new Occurrence("ΤΕ", "17-19", "ΗΥ1 & ΗΥ2")),
            new Lesson("Σχεδίαση Ψηφιακών Συστημάτων (Φροντιστήριο)",                             new Occurrence("ΔΕ", "15-17", "ΗΥ1 & ΗΥ2"), new Occurrence("ΔΕ", "17-19", "ΗΥ1 & ΗΥ2"), new Occurrence("ΤΕ", "11-13", "Αμφ.Α"), new Occurrence("ΠΕ", "15-17", "ΗΥ1 & ΗΥ2"), new Occurrence("ΠΕ", "17-19", "ΗΥ1 & ΗΥ2")),

            // 4th Semester
            new Lesson("Λειτουργικά Συστήματα",                                                   new Occurrence("ΤΡ", "13-15", "Χ"),         new Occurrence("ΠΑ", "13-15", "Χ")),
            new Lesson("Βάσεις Δεδομένων",                                                        new Occurrence("ΔΕ", "15-17", "Αμφ.Α"),     new Occurrence("ΤΡ", "15-17", "Χ")),
            new Lesson("Αλγόριθμοι",                                                              new Occurrence("ΔΕ", "11-13", "Χ"),         new Occurrence("ΤΡ", "11-13", "Χ")),
            new Lesson("Θεωρία Υπολογισμού",                                                      new Occurrence("ΔΕ", "9-11",  "Χ"),         new Occurrence("ΤΡ", "9-11", "Χ")),
            
            new Lesson("Λειτουργικά Συστήματα (Εργαστήριο)",                                      new Occurrence("ΤΕ", "9-11",  "CSLab2"),    new Occurrence("ΠΕ", "9-11", "CSLab2")),
            new Lesson("Αλγόριθμοι (Φροντιστήριο)",                                               new Occurrence("ΠΑ", "17-19", "Χ")),
            new Lesson("Θεωρία Υπολογισμού (Φροντιστήριο)",                                       new Occurrence("ΠΑ", "15-17", "Αμφ.Β")),
            new Lesson("Λειτουργικά Συστήματα (Φροντιστήριο)",                                    new Occurrence("ΤΕ", "13-15", "Χ")),

            // 6th Semester
            new Lesson("Κατανεμημένα Συστήματα",                                                  new Occurrence("ΔΕ", "9-11",  "Αμφ.Γ"),     new Occurrence("ΠΑ", "11-13", "Αμφ.Β")),
            new Lesson("Θεωρία και Υποδείγματα Βελτιστοποίησης",                                  new Occurrence("ΔΕ", "11-13", "Α47"),       new Occurrence("ΤΡ", "15-17", "Υ3")),
            new Lesson("Δίκτυα Υπολογιστών",                                                      new Occurrence("ΤΡ", "9-11",  "Δ24"),       new Occurrence("ΠΕ", "11-13", "Α21")),
            new Lesson("Κυβερνοασφάλεια",                                                         new Occurrence("ΤΡ", "17-19", "Α31"),       new Occurrence("ΠΑ", "15-17", "Δ22")),
            new Lesson("Επαλήθευση, Επικύρωση και Συντήρηση Λογισμικού",                          new Occurrence("ΔΕ", "15-17", "Χ"),         new Occurrence("ΠΑ", "17-19", "Αμφ.Α")),
            new Lesson("Συστήματα Διαχείρισης και Ανάλυσης Δεδομένων",                            new Occurrence("ΔΕ", "13-15", "Α24"),       new Occurrence("ΠΕ", "13-15", "Α24")),
            new Lesson("Αριθμητική Γραμμική Άλγεβρα",                                             new Occurrence("ΤΡ", "19-15", "Τ101"),      new Occurrence("ΠΑ", "13-15", "Τ103")),
            new Lesson("Ανάλυση και Σχεδίαση Πληροφοριακών Συστημάτων",                           new Occurrence("ΤΕ", "9-11",  "Α21"),       new Occurrence("ΠΕ", "9-11",  "Α23")),
          
            new Lesson("Κατανεμημένα Συστήματα (Φροντιστήριο) | 1ο Τμήμα",                        new Occurrence("ΤΕ", "11-13", "CSLab1"),    new Occurrence("ΠΕ", "15-17", "CSLab1")),
            new Lesson("Κατανεμημένα Συστήματα (Φροντιστήριο) | 2ο Τμήμα",                        new Occurrence("ΤΕ", "13-15", "CSLab1"),    new Occurrence("ΠΕ", "17-19", "CSLab1")),
            new Lesson("Δίκτυα Υπολογιστών (Φροντιστήριο)",                                       new Occurrence("ΤΕ", "15-17", "Χ")),
            new Lesson("Επαλήθευση, Επικύρωση και Συντήρηση Λογισμικού (Φροντιστήριο)",           new Occurrence("ΔΕ", "5-9", "Teams")),
            new Lesson("Ανάλυση και Σχεδίαση Πληροφοριακών Συστημάτων (Φροντιστήριο)",            new Occurrence("ΤΡ", "11-1", "Δ22")),
            new Lesson("Θεωρία και Υποδείγματα Βελτιστοποίησης (Φροντιστήριο)",                   new Occurrence("ΤΕ", "13-15", "Α21")),
            new Lesson("Συστήματα Διαχείρισης και Ανάλυσης Δεδομένων (Φροντιστήριο) | 1ο Τμήμα",  new Occurrence("ΤΕ", "17-19", "Χ")),
            new Lesson("Συστήματα Διαχείρισης και Ανάλυσης Δεδομένων (Φροντιστήριο) | 2ο Τμήμα",  new Occurrence("ΠΑ", "9-11", "Α25")),
            new Lesson("Αριθμητική Γραμμική Άλγεβρα (Φροντιστήριο)",                              new Occurrence("ΠΑ", "15-17", "Τ103")),

            // 8th Semester
            new Lesson("Αλληλεπίδραση Ανθρώπου - Υπολογιστή",                                     new Occurrence("ΔΕ", "13-15", "Α47"),       new Occurrence("ΠΑ", "13-15", "Τ203")),
            new Lesson("Ανάλυση Επίδοσης Πολύπλοκων Δικτυωμένων Συστημάτων",                      new Occurrence("ΔΕ", "11-13", "Τ103"),      new Occurrence("ΠΕ", "11-13", "Τ103")),
            new Lesson("Ανάπτυξη Εφαρμογών Πληροφοριακών Συστημάτων",                             new Occurrence("ΔΕ", "15-17", "Τ101"),      new Occurrence("ΠΕ", "11-13", "Τ101")),
            new Lesson("Αρχιτεκτονική Υπολογιστών",                                               new Occurrence("ΤΡ", "9-11",  "Τ102"),      new Occurrence("ΠΑ", "9-11",  "Τ102")),
            new Lesson("Ασφάλεια Δικτύων",                                                        new Occurrence("ΤΕ", "17-19", "Α22"),       new Occurrence("ΠΕ", "15-17", "Τ107")),
            new Lesson("Εννοιολογική Μοντελοποίηση και Οργάνωση Γνώσεων",                         new Occurrence("ΤΡ", "17-19", "Α25"),       new Occurrence("ΤΕ", "15-17", "Α47")),
            new Lesson("Εξόρυξη Γνώσης και Επιστήμη Δεδομένων",                                   new Occurrence("ΤΡ", "13-15", "Δ22"),       new Occurrence("ΠΕ", "13-15", "Υ1")),
            new Lesson("Θεωρία Παιγνίων & Αποφάσεων",                                             new Occurrence("ΔΕ", "15-17", "Τ105"),      new Occurrence("ΠΕ", "15-17", "Τ105")),
            new Lesson("Παράλληλος Προγραμματισμός",                                              new Occurrence("ΤΡ", "19-21", "Α44"),       new Occurrence("ΠΕ", "13-15", "Τ103")),
            new Lesson("Σήματα, Συστήματα και Ψηφιακή Επεξεργασία Σημάτων",                       new Occurrence("ΤΕ", "19-21", "Α22"),       new Occurrence("ΠΑ", "15-17", "Υ1")),
            new Lesson("Στοιχεία Δικαίου της Πληροφορίας",                                        new Occurrence("ΔΕ", "9-11",  "Α25"),       new Occurrence("ΤΕ", "9-11",  "Υ3")),
            new Lesson("Τεχνολογία Πολυμέσων",                                                    new Occurrence("ΔΕ", "13-15", "Υ3"),        new Occurrence("ΤΡ", "11-13", "Α32")),
            new Lesson("Τεχνολογική Καινοτομία καί Επιχειρηματικότητα",                           new Occurrence("ΔΕ", "13-17", "Δο")),
            new Lesson("Υπολογισιμότητα & Πολυπλοκότητα",                                         new Occurrence("ΠΕ", "9-11",  "Τ101"),      new Occurrence("ΠΑ", "9-11",  "Τ103")),
            
            new Lesson("Αλληλεπίδραση Ανθρώπου - Υπολογιστή (Φροντιστήριο)",                      new Occurrence("ΠΕ", "19-21",  "Α32")),
            new Lesson("Αρχιτεκτονική Υπολογιστών (Φροντιστήριο)",                                new Occurrence("ΤΕ", "13-15",  "CSLab2")),
            new Lesson("Εννοιολογική Μοντελοποίηση και Οργάνωση Γνώσεων (Φροντιστήριο)",          new Occurrence("ΠΕ", "17-19",  "Δ102")),
            new Lesson("Θεωρία Παιγνίων & Αποφάσεων (Φροντιστήριο)",                              new Occurrence("ΔΕ", "17-19",  "Α32")),

            new Lesson("Γενική και Εξελικτική Ψυχολογία",                                         new Occurrence("ΤΕ", "15-17", "Δ102")),
            new Lesson("Ποιότητα στην Εκπαίδευση και τη Διδασκαλία",                              new Occurrence("ΤΡ", "17-19", "Α23")),
            new Lesson("Ειδική Διδακτική Μεθοδολογία - Διδακτική Πληροφορικής",                   new Occurrence("ΤΕ", "9-11",  "Τ103")),
            new Lesson("Εισαγωγή στους ΗΥ- Παιδαγωγικές Εφαρμογές στην Εκπαίδευση",               new Occurrence("ΤΡ", "15-17", "ΗΥ1")),
            new Lesson("Πρακτική Άσκηση στη Διδασκαλία (ΠΑΔ) ΙΙ",                                 new Occurrence("ΠΑ", "11-17", "Υ3"))
        );
}