package com.example.remindme.util

import com.example.remindme.data.MedicineSuggestion

object MedicineSuggestions {
    private val medicines = listOf(
        // Pain and Fever
        MedicineSuggestion("Paracetamol (Crocin/Dolo)", "Fever, Pain, Headache"),
        MedicineSuggestion("Ibuprofen (Brufen)", "Pain, Inflammation"),
        MedicineSuggestion("Diclofenac (Voveran)", "Pain, Arthritis"),
        
        // Cold and Allergies
        MedicineSuggestion("Cetirizine (Alerid)", "Allergies, Cold"),
        MedicineSuggestion("Montelukast (Montair)", "Asthma, Allergies"),
        MedicineSuggestion("Levocetrizine (Xyzal)", "Allergies, Rhinitis"),
        MedicineSuggestion("Chlorpheniramine (Piriton)", "Allergies, Cold"),
        
        // Antibiotics
        MedicineSuggestion("Amoxicillin (Novamox)", "Bacterial infections"),
        MedicineSuggestion("Azithromycin (Zithromax)", "Bacterial infections"),
        MedicineSuggestion("Ciprofloxacin (Ciplox)", "Bacterial infections"),
        MedicineSuggestion("Doxycycline (Doxy)", "Bacterial infections"),
        
        // Diabetes
        MedicineSuggestion("Metformin (Glycomet)", "Diabetes Type 2"),
        MedicineSuggestion("Glimepiride (Amaryl)", "Diabetes"),
        MedicineSuggestion("Sitagliptin (Januvia)", "Diabetes Type 2"),
        
        // Blood Pressure
        MedicineSuggestion("Amlodipine (Amlong)", "High blood pressure"),
        MedicineSuggestion("Telmisartan (Telma)", "High blood pressure"),
        MedicineSuggestion("Metoprolol (Metolar)", "High blood pressure, Angina"),
        MedicineSuggestion("Losartan (Losartan)", "High blood pressure"),
        
        // Gastric/Digestive
        MedicineSuggestion("Omeprazole (Omez)", "Acidity, Ulcers"),
        MedicineSuggestion("Pantoprazole (Pan-40)", "Acidity, GERD"),
        MedicineSuggestion("Ranitidine (Rantac)", "Acidity, Ulcers"),
        MedicineSuggestion("Domperidone (Domstal)", "Nausea, Vomiting"),
        
        // Vitamins and Supplements
        MedicineSuggestion("Vitamin B Complex (Becosules)", "Vitamin deficiency"),
        MedicineSuggestion("Calcium + Vitamin D3 (Shelcal)", "Calcium deficiency"),
        MedicineSuggestion("Iron + Folic Acid (Feronia-XT)", "Anemia"),
        
        // Mental Health
        MedicineSuggestion("Alprazolam (Alprax)", "Anxiety"),
        MedicineSuggestion("Escitalopram (Nexito)", "Depression, Anxiety"),
        MedicineSuggestion("Sertraline (Zoloft)", "Depression"),
        
        // Heart Related
        MedicineSuggestion("Aspirin (Ecosprin)", "Blood thinning, Heart disease"),
        MedicineSuggestion("Atorvastatin (Atorva)", "High cholesterol"),
        MedicineSuggestion("Clopidogrel (Clopilet)", "Blood thinning"),
        
        // Thyroid
        MedicineSuggestion("Levothyroxine (Eltroxin)", "Hypothyroidism"),
        MedicineSuggestion("Thyroxine (Thyronorm)", "Thyroid hormone replacement"),
        
        // Respiratory
        MedicineSuggestion("Salbutamol (Asthalin)", "Asthma, Breathing problems"),
        MedicineSuggestion("Theophylline (Duralyn)", "Asthma, COPD"),
        MedicineSuggestion("Budesonide (Budecort)", "Asthma, Allergic rhinitis"),
        
        // Anti-inflammatory
        MedicineSuggestion("Prednisolone (Wysolone)", "Inflammation, Allergies"),
        MedicineSuggestion("Deflazacort (Defcort)", "Inflammation, Auto-immune conditions"),
        
        // Skin Related
        MedicineSuggestion("Betamethasone (Betnovate)", "Skin conditions"),
        MedicineSuggestion("Clotrimazole (Candid)", "Fungal infections"),
        MedicineSuggestion("Mupirocin (Bactroban)", "Bacterial skin infections"),
        
        // Others
        MedicineSuggestion("Ondansetron (Emeset)", "Nausea, Vomiting"),
        MedicineSuggestion("Dicyclomine (Cyclopam)", "Abdominal pain, Spasms"),
        MedicineSuggestion("Metronidazole (Flagyl)", "Bacterial/Parasitic infections"),
        
        // Eye/Ear Drops
        MedicineSuggestion("Tropicamide (Tropicacyl)", "Eye examination, Dilation"),
        MedicineSuggestion("Ciprofloxacin Drops (Ciplox)", "Eye/Ear infections"),
        MedicineSuggestion("Moxifloxacin (Moxicip)", "Eye infections"),
        MedicineSuggestion("Tobramycin (Tobrex)", "Eye infections"),
        
        // Sleep/Insomnia
        MedicineSuggestion("Zolpidem (Stilnoct)", "Insomnia"),
        MedicineSuggestion("Melatonin (N-Mel)", "Sleep regulation"),
        MedicineSuggestion("Zopiclone (Zopicon)", "Insomnia"),
        
        // Muscle Relaxants
        MedicineSuggestion("Baclofen (Lioresal)", "Muscle spasms"),
        MedicineSuggestion("Methocarbamol (Robaxin)", "Muscle pain"),
        MedicineSuggestion("Tizanidine (Tizan)", "Muscle spasticity"),
        
        // Women's Health
        MedicineSuggestion("Folic Acid (Folvite)", "Pregnancy supplement"),
        MedicineSuggestion("Progesterone (Susten)", "Hormonal support"),
        MedicineSuggestion("Clomiphene (Clomid)", "Fertility treatment"),
        MedicineSuggestion("Tranexamic Acid (Pause)", "Menstrual bleeding"),
        
        // Urological
        MedicineSuggestion("Tamsulosin (Flomax)", "Prostate enlargement"),
        MedicineSuggestion("Sildenafil (Viagra)", "Erectile dysfunction"),
        MedicineSuggestion("Dutasteride (Avodart)", "Prostate problems"),
        
        // Anti-Viral
        MedicineSuggestion("Acyclovir (Zovirax)", "Viral infections"),
        MedicineSuggestion("Oseltamivir (Tamiflu)", "Influenza"),
        MedicineSuggestion("Valacyclovir (Valcivir)", "Herpes infections"),
        
        // Anti-Fungal
        MedicineSuggestion("Fluconazole (Forcan)", "Fungal infections"),
        MedicineSuggestion("Itraconazole (Itrasys)", "Severe fungal infections"),
        MedicineSuggestion("Terbinafine (Terbicip)", "Fungal skin infections"),
        
        // Migraine
        MedicineSuggestion("Sumatriptan (Suminat)", "Migraine"),
        MedicineSuggestion("Rizatriptan (Rizact)", "Migraine"),
        MedicineSuggestion("Propranolol (Ciplar)", "Migraine prevention"),
        
        // Bone Health
        MedicineSuggestion("Alendronate (Fosamax)", "Osteoporosis"),
        MedicineSuggestion("Calcitriol (Rocaltrol)", "Vitamin D supplement"),
        MedicineSuggestion("Risedronate (Actonel)", "Osteoporosis"),
        
        // Anti-Emetic
        MedicineSuggestion("Domperidone (Motilium)", "Nausea, Vomiting"),
        MedicineSuggestion("Prochlorperazine (Stemetil)", "Severe vomiting"),
        MedicineSuggestion("Metoclopramide (Perinorm)", "Nausea, GERD"),
        
        // Pediatric Specific
        MedicineSuggestion("Zinc Supplement (Zincovit)", "Child growth"),
        MedicineSuggestion("ORS (Electral)", "Dehydration"),
        MedicineSuggestion("Paracetamol Syrup (Crocin)", "Child fever"),
        MedicineSuggestion("Amoxicillin Syrup (Novamox)", "Child infections"),
        
        // Dental/Oral Care
        MedicineSuggestion("Chlorhexidine (Hexidine)", "Mouth ulcers, Gum disease"),
        MedicineSuggestion("Benzocaine (Mucopain)", "Tooth pain"),
        MedicineSuggestion("Lignocaine Gel (Xylocaine)", "Dental pain"),
        MedicineSuggestion("Metronidazole (Flagyl Gel)", "Dental infections"),
        
        // Weight Management
        MedicineSuggestion("Orlistat (Xenical)", "Weight loss"),
        MedicineSuggestion("Liraglutide (Saxenda)", "Weight management"),
        MedicineSuggestion("Phentermine (Duromine)", "Appetite control"),
        
        // Hormonal Treatment
        MedicineSuggestion("Testosterone (Androgel)", "Hormone replacement"),
        MedicineSuggestion("Estradiol (Progynova)", "Hormone replacement"),
        MedicineSuggestion("Methylprednisolone (Medrol)", "Hormone disorders"),
        
        // Neurological
        MedicineSuggestion("Gabapentin (Gabapin)", "Nerve pain"),
        MedicineSuggestion("Pregabalin (Lyrica)", "Neuropathic pain"),
        MedicineSuggestion("Levetiracetam (Keppra)", "Epilepsy"),
        MedicineSuggestion("Donepezil (Aricept)", "Alzheimer's"),
        
        // Immunosuppressants
        MedicineSuggestion("Tacrolimus (Prograf)", "Organ transplant"),
        MedicineSuggestion("Cyclosporine (Sandimmun)", "Auto-immune conditions"),
        MedicineSuggestion("Mycophenolate (CellCept)", "Transplant rejection"),
        
        // Vaccines
        MedicineSuggestion("Influenza Vaccine (FluShot)", "Flu prevention"),
        MedicineSuggestion("Pneumococcal Vaccine (Pneumovax)", "Pneumonia prevention"),
        MedicineSuggestion("Hepatitis B Vaccine (Engerix)", "Hepatitis B prevention"),
        
        // Topical Medications
        MedicineSuggestion("Ketoconazole (Nizoral)", "Dandruff, Fungal skin"),
        MedicineSuggestion("Calamine Lotion", "Skin irritation, Rashes"),
        MedicineSuggestion("Permethrin (Permite)", "Scabies, Lice"),
        
        // Nasal Preparations
        MedicineSuggestion("Xylometazoline (Otrivin)", "Nasal congestion"),
        MedicineSuggestion("Fluticasone (Flonase)", "Allergic rhinitis"),
        MedicineSuggestion("Ipratropium (Nasivin)", "Runny nose"),
        
        // Emergency Medicines
        MedicineSuggestion("Nitroglycerin (Sorbitrate)", "Angina, Chest pain"),
        MedicineSuggestion("Adrenaline (EpiPen)", "Severe allergic reactions"),
        MedicineSuggestion("Glucagon (GlucaGen)", "Severe hypoglycemia"),
        
        // Gout Medications
        MedicineSuggestion("Allopurinol (Zyloric)", "Gout prevention"),
        MedicineSuggestion("Colchicine (Colchin)", "Gout attacks"),
        MedicineSuggestion("Febuxostat (Febutaz)", "Chronic gout")
    )

    fun searchMedicines(query: String): List<MedicineSuggestion> {
        if (query.length < 2) return emptyList()
        return medicines.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.commonUses.contains(query, ignoreCase = true)
        }
    }
} 