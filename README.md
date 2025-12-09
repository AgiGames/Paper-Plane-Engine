# Paper Plane's Print Engine 🖨️⚙️

### How to Start the Engine 🛠️

- ### Windows 🪟
    Step 1: Download the zip file of the latest release ```Paper_Plane_Engine.zip``` from github in releases section.
    
    Step 2: Place the zip file anywhere you like, and extract it.
    
    Step 3: Open the folder obtained after extracting the zip file.
    
    Step 4: Keep opening the sub-folders until you see a file named ```run-paper-plane-engine``` or ```run-paper-plane-engine.bat```.
    
    Step 5: Double click ```run-paper-plane-engine``` or ```run-paper-plane-engine.bat```.
    
    Step 6: Engine is now running!

---

## 🚀 Features

* Upload and print files with custom page dimensions (in millimeters)
* Color or monochrome printing
* List all available printers on the host machine
* Exclude certain printers dynamically
* Cross-origin friendly (CORS enabled)

---

## 📌 API Endpoints

### ### **1. Print a File**

`POST /ppe/print`

Uploads a file and sends it to the printer.

#### **Parameters (multipart/form-data)**

| Name        | Type    | Required | Description                |
| ----------- | ------- | -------- | -------------------------- |
| `file`      | File    | Yes      | The file to print          |
| `color`     | boolean | Yes      | Use color printing if true |
| `width-mm`  | float   | Yes      | Page width in millimeters  |
| `height-mm` | float   | Yes      | Page height in millimeters |

#### **JavaScript Example**

```js
async function printFile(file, color, widthMM, heightMM) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("color", color);
    formData.append("width-mm", widthMM);
    formData.append("height-mm", heightMM);

    const res = await fetch("http://localhost:8080/ppe/print", {
        method: "POST",
        body: formData
    });

    console.log(await res.text());
}
```

---

### **2. Get Available Printers**

`POST /ppe/get-printers`

Returns all discovered printer names.

#### **Response Example**

```json
[
  "HP LaserJet Pro M404",
  "Canon G3010 Series",
  "Microsoft Print to PDF"
]
```

#### **JS Example**

```js
async function getPrinters() {
    const res = await fetch("http://localhost:8080/ppe/get-printers", { method: "POST" });
    return await res.json();
}
```

---

### **3. Exclude Specific Printers**

`POST /ppe/exclude-printers`

#### **Parameters (multipart/form-data)**

| Name                | Type     | Description                 |
| ------------------- | -------- | --------------------------- |
| `printer-names`     | string[] | Printers to exclude         |
| `clear-exclude-set` | boolean  | Reset exclusion set if true |

#### **JS Example**

```js
async function excludePrinters(names, clear=false) {
    const formData = new FormData();
    names.forEach(n => formData.append("printer-names", n));
    formData.append("clear-exclude-set", clear);

    const res = await fetch("http://localhost:8080/ppe/exclude-printers", {
        method: "POST",
        body: formData
    });

    console.log(await res.text());
}
```

---
