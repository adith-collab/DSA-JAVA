import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import time
import os

# --- Doctor and Patient Classes ---
class Doctor:
    def _init_(self, name, occupation):
        self.name = name
        self.occupation = occupation
        self.patient_queue = []
        self.available = True

    def add_patient(self, patient):
        self.patient_queue.append(patient)
        self.patient_queue.sort(key=lambda p: (p.severity, p.arrival_time))
        self.available = False

    def _str_(self):
        status = "Available" if self.available else "Busy"
        return f"{self.name} ({self.occupation}) - {status} - Patients: {len(self.patient_queue)}"

class Patient:
    def _init_(self, name, severity, doctor):
        self.name = name
        self.severity = severity
        self.doctor = doctor
        self.arrival_time = time.time()
        self.status = "Waiting"

    def get_waiting_time(self):
        return int(time.time() - self.arrival_time)

    def _str_(self):
        wait_time = self.get_waiting_time()
        return f"{self.name} | Severity: {self.severity} | Doctor: {self.doctor.name} | Wait: {wait_time}s | Status: {self.status}"

# --- Hospital Queue App Class ---
class HospitalQueueApp:
    def _init_(self, root):
        self.root = root
        self.root.title("Hospital Queue Management System")
        self.root.state('zoomed')  # Maximize window

        self.doctors = []
        self.patient_names = set()

        self.create_doctors()
        self.build_ui()

    def create_doctors(self):
        doctor_details = [
            ("Dr. Smith", "Cardiologist"), ("Dr. Patel", "Neurologist"), ("Dr. Wang", "Pediatrician"),
            ("Dr. Brown", "Dermatologist"), ("Dr. Singh", "Orthopedic"), ("Dr. Khan", "Gynecologist"),
            ("Dr. Lee", "Psychiatrist"), ("Dr. Adams", "Surgeon"), ("Dr. Clark", "ENT"),
            ("Dr. Das", "General Physician")
        ]
        self.doctors = [Doctor(name, occ) for name, occ in doctor_details]

    def build_ui(self):
        self.style = ttk.Style()
        self.style.theme_use("clam")

        # Main horizontal split layout
        self.pane = ttk.PanedWindow(self.root, orient=tk.HORIZONTAL)
        self.pane.pack(fill=tk.BOTH, expand=True)

        # Left side panel for form and controls
        left_frame = ttk.Frame(self.pane, padding=20)
        self.pane.add(left_frame, weight=1)

        # Right panel for display
        right_frame = ttk.Frame(self.pane, padding=10)
        self.pane.add(right_frame, weight=3)

        # --- Input Form ---
        form_frame = ttk.LabelFrame(left_frame, text="Patient Information", padding=10)
        form_frame.pack(fill='x', pady=10)

        ttk.Label(form_frame, text="Patient Name:").grid(row=0, column=0, sticky='w')
        self.name_entry = ttk.Entry(form_frame, width=25)
        self.name_entry.grid(row=0, column=1, pady=5)

        ttk.Label(form_frame, text="Severity (1-4):").grid(row=1, column=0, sticky='w')
        self.severity_entry = ttk.Entry(form_frame, width=25)
        self.severity_entry.grid(row=1, column=1, pady=5)

        ttk.Label(form_frame, text="Select Doctor:").grid(row=2, column=0, sticky='w')
        self.doctor_combo = ttk.Combobox(form_frame, state="readonly", width=23)
        self.doctor_combo['values'] = [f"{d.name} ({d.occupation})" for d in self.doctors]
        self.doctor_combo.current(0)
        self.doctor_combo.grid(row=2, column=1, pady=5)

        ttk.Button(form_frame, text="Add Patient", command=self.add_patient).grid(row=3, column=0, columnspan=2, pady=10)

        # --- Controls ---
        control_frame = ttk.LabelFrame(left_frame, text="Actions", padding=10)
        control_frame.pack(fill='x', pady=10)

        ttk.Button(control_frame, text="Attend Next Patient", command=self.attend_patient).pack(fill='x', pady=5)
        ttk.Button(control_frame, text="Show Full Queue", command=self.display_queue).pack(fill='x', pady=5)
        ttk.Button(control_frame, text="Show Assigned Patients", command=self.display_assigned).pack(fill='x', pady=5)
        ttk.Button(control_frame, text="Save to File", command=self.save_data).pack(fill='x', pady=5)
        ttk.Button(control_frame, text="Load from File", command=self.load_data).pack(fill='x', pady=5)
        ttk.Button(control_frame, text="Statistics Report", command=self.show_statistics).pack(fill='x', pady=5)

        ttk.Label(control_frame, text="Select Theme:").pack(pady=(10, 2))
        self.theme_var = tk.StringVar()
        self.theme_combo = ttk.Combobox(control_frame, state="readonly", values=["Light", "Dark", "Blue"], textvariable=self.theme_var)
        self.theme_combo.current(0)
        self.theme_combo.pack(fill='x')
        self.theme_combo.bind("<<ComboboxSelected>>", self.change_theme)

        # --- Text area in right panel ---
        self.text_area = tk.Text(right_frame, wrap="word", font=("Consolas", 11))
        self.text_area.pack(fill='both', expand=True)
        self.text_area.config(height=30)

    def add_patient(self):
        name = self.name_entry.get().strip()
        severity = self.severity_entry.get().strip()
        if not name.isalpha() or name in self.patient_names:
            messagebox.showerror("Error", "Invalid or duplicate patient name.")
            return
        if not severity.isdigit() or not 1 <= int(severity) <= 4:
            messagebox.showerror("Error", "Severity must be between 1 and 4.")
            return
        doctor = self.doctors[self.doctor_combo.current()]
        patient = Patient(name, int(severity), doctor)
        doctor.add_patient(patient)
        self.patient_names.add(name)
        self.name_entry.delete(0, tk.END)
        self.severity_entry.delete(0, tk.END)
        self.display_queue()

    def attend_patient(self):
        for doc in self.doctors:
            for p in doc.patient_queue:
                if p.status == "Waiting":
                    p.status = "Served"
                    doc.available = not any(p.status == "Waiting" for p in doc.patient_queue)
                    self.display_queue()
                    return
        messagebox.showinfo("Info", "No waiting patients.")

    def display_queue(self):
        self.text_area.delete(1.0, tk.END)
        self.text_area.insert(tk.END, "=== Doctor Status ===\n")
        for doc in self.doctors:
            self.text_area.insert(tk.END, str(doc) + "\n")
        self.text_area.insert(tk.END, "\n=== Patient Queue ===\n")
        any_patients = False
        for doc in self.doctors:
            for p in doc.patient_queue:
                self.text_area.insert(tk.END, str(p) + "\n")
                any_patients = True
        if not any_patients:
            self.text_area.insert(tk.END, "No patients in the queue.\n")

    def display_assigned(self):
        self.text_area.delete(1.0, tk.END)
        for doc in self.doctors:
            self.text_area.insert(tk.END, f"\n--- {doc.name} ({doc.occupation}) ---\n")
            if not doc.patient_queue:
                self.text_area.insert(tk.END, "No patients assigned.\n")
            else:
                for p in doc.patient_queue:
                    self.text_area.insert(tk.END, f"{p.name} ({p.status})\n")

    def show_statistics(self):
        served, total_wait = 0, 0
        for doc in self.doctors:
            for p in doc.patient_queue:
                if p.status == "Served":
                    served += 1
                    total_wait += p.get_waiting_time()
        msg = "No patients served yet." if served == 0 else f"Patients served: {served}\nAvg wait time: {total_wait // served} sec"
        messagebox.showinfo("Statistics", msg)

    def save_data(self):
        filepath = filedialog.asksaveasfilename(defaultextension=".txt")
        if filepath:
            try:
                with open(filepath, "w") as f:
                    for doc in self.doctors:
                        for p in doc.patient_queue:
                            f.write(f"{p.name},{p.severity},{int(p.arrival_time)},{p.status},{doc.name}\n")
                messagebox.showinfo("Saved", "Data saved successfully.")
            except:
                messagebox.showerror("Error", "Failed to save data.")

    def load_data(self):
        filepath = filedialog.askopenfilename()
        if filepath and os.path.exists(filepath):
            for doc in self.doctors:
                doc.patient_queue.clear()
                doc.available = True
            self.patient_names.clear()
            with open(filepath, "r") as f:
                for line in f:
                    name, severity, arr_time, status, doc_name = line.strip().split(",", 4)
                    doc = next((d for d in self.doctors if d.name == doc_name), None)
                    if doc:
                        patient = Patient(name, int(severity), doc)
                        patient.arrival_time = float(arr_time)
                        patient.status = status
                        doc.add_patient(patient)
                        if status == "Waiting":
                            doc.available = False
                        self.patient_names.add(name)
            self.display_queue()

    def change_theme(self, _=None):
        theme = self.theme_var.get()
        if theme == "Dark":
            self.text_area.config(bg="black", fg="lime")
        elif theme == "Blue":
            self.text_area.config(bg="#e0f7fa", fg="blue")
        else:
            self.text_area.config(bg="white", fg="black")

# --- Main Program Execution ---
if _name_ == "_main_":
    root = tk.Tk()
    app = HospitalQueueApp(root)
    root.mainloop()