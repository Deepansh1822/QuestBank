document.addEventListener('DOMContentLoaded', () => {
    let currentStep = 1;
    const totalSteps = 5;



    const nextBtn = document.getElementById('nextBtn');
    const prevBtn = document.getElementById('prevBtn');
    const finalSubmitBtn = document.getElementById('finalSubmitBtn');
    const qCountSlider = document.getElementById('qCount');
    const qCountVal = document.getElementById('qCountVal');
    const marksSlider = document.getElementById('marks');
    const marksVal = document.getElementById('marksVal');

    // === Premium Custom Dropdown Logic ===
    const dropdownData = {}; // Store raw data for each category

    async function initAutocomplete() {
        const endpoints = {
            'organisation': '/api/data/organisations',
            'className': '/api/data/classes',
            'subjectName': '/api/data/subjects',
            'chapterName': '/api/data/chapters',
            'topicName': '/api/data/topics'
        };

        await Promise.all(Object.entries(endpoints).map(async ([fieldId, url]) => {
            try {
                const resp = await fetch(url);
                if (resp.ok) {
                    const items = await resp.json();
                    dropdownData[fieldId] = [...new Set(items.filter(i => i && i.trim() !== ""))];

                    // Bind to existing inputs on page
                    const input = document.getElementById(fieldId);
                    if (input) setupCustomDropdown(input, fieldId);
                }
            } catch (err) { console.error('Error fetching ' + fieldId, err); }
        }));
    }

    function setupCustomDropdown(input, dataKey) {
        if (input.dataset.dropdownReady) return;
        input.dataset.dropdownReady = "true";
        input.setAttribute('autocomplete', 'off');

        // Create Wrapper & Menu
        const wrapper = document.createElement('div');
        wrapper.className = 'autocomplete-container';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const menu = document.createElement('div');
        menu.className = 'custom-dropdown-menu';
        wrapper.appendChild(menu);

        const updateMenu = (filter = "") => {
            const data = dropdownData[dataKey] || [];
            const filtered = data.filter(item => item.toLowerCase().includes(filter.toLowerCase()));

            if (filtered.length === 0) {
                menu.style.display = 'none';
                return;
            }

            menu.innerHTML = filtered.map(item => `
                <div class="custom-dropdown-item" data-value="${item}">
                    <i class="fas fa-database"></i>
                    <span>${item}</span>
                </div>
            `).join('');
            menu.style.display = 'block';

            menu.querySelectorAll('.custom-dropdown-item').forEach(item => {
                item.addEventListener('mousedown', (e) => {
                    input.value = item.dataset.value;
                    input.dispatchEvent(new Event('input')); // Trigger logo fetch etc
                    menu.style.display = 'none';
                });
            });
        };

        input.addEventListener('focus', () => updateMenu(input.value));
        input.addEventListener('input', () => updateMenu(input.value));
        input.addEventListener('blur', () => {
            // Delay to allow mousedown on items
            setTimeout(() => { menu.style.display = 'none'; }, 200);
        });
    }

    initAutocomplete();

    const diffOptions = document.querySelectorAll('.difficulty-option');
    const typeOptions = document.querySelectorAll('.type-option');
    const sourceOptions = document.querySelectorAll('.source-card');

    // Navigation
    nextBtn.addEventListener('click', () => {
        if (currentStep < totalSteps) {
            currentStep++;
            updateUI();
            if (currentStep === 3) generateManualInputs();
        }
    });

    prevBtn.addEventListener('click', () => {
        if (currentStep > 1) {
            currentStep--;
            updateUI();
        }
    });

    // Sliders
    function handleSlider(slider, display) {
        slider.addEventListener('input', (e) => {
            display.textContent = e.target.value;
            const value = (slider.value - slider.min) / (slider.max - slider.min) * 100;
            slider.style.backgroundSize = `${value}% 100%`;
            if (currentStep === 3) generateManualInputs();
        });
        slider.dispatchEvent(new Event('input'));
    }
    handleSlider(qCountSlider, qCountVal);
    handleSlider(marksSlider, marksVal);

    // Selections
    function handleSelection(options, className, hiddenInputId) {
        options.forEach(opt => {
            if (opt.classList.contains(className)) {
                const hidden = document.getElementById(hiddenInputId);
                if (hidden) hidden.value = opt.dataset.value;
            }
            opt.addEventListener('click', () => {
                options.forEach(o => o.classList.remove('selected', 'active'));
                opt.classList.add('selected', 'active');
                if (hiddenInputId) {
                    const hidden = document.getElementById(hiddenInputId);
                    if (hidden) hidden.value = opt.dataset.value;
                }
                if (currentStep === 3) {
                    const method = opt.dataset.method;
                    if (method) {
                        document.querySelectorAll('.method-content').forEach(el => el.classList.add('hidden'));
                        document.getElementById(`method-${method}`).classList.remove('hidden');
                        if (method === 'manual') generateManualInputs();
                    }
                }
            });
        });
    }
    handleSelection(diffOptions, 'selected', 'difficultyInput');
    handleSelection(typeOptions, 'selected', 'typeInput');
    handleSelection(sourceOptions, 'active', 'sourceInput');

    // Dynamic Logic
    function generateManualInputs() {
        const container = document.getElementById('method-manual');
        const count = parseInt(qCountSlider.value) || 10;
        const type = document.getElementById('typeInput').value || 'MCQ';
        container.innerHTML = '';
        container.classList.remove('hidden');

        const readableType = type.replace(/_/g, " ");
        const header = document.createElement('div');
        header.innerHTML = `<h3 style="margin-bottom:1.5rem; color:var(--primary-color)">Enter ${count} ${readableType} Questions</h3>`;
        container.appendChild(header);

        for (let i = 1; i <= count; i++) {
            const wrapper = document.createElement('div');
            wrapper.className = "question-block";
            wrapper.style.cssText = "background:rgba(255,255,255,0.02); padding:1.5rem; border-radius:12px; margin-bottom:1.5rem; border:1px solid var(--glass-border);";
            let innerHTML = `<div class="input-group"><label style="color:var(--secondary-color)">Question ${i}</label><textarea rows="2" placeholder="Type question text..." class="q-input" name="questions[${i - 1}].question"></textarea></div>`;
            if (type === 'MCQ') {
                innerHTML += `<div style="display:grid; grid-template-columns: 1fr 1fr; gap:1rem; margin-bottom:1rem;">
                    <div><input type="text" placeholder="Option A" name="questions[${i - 1}].option1"></div>
                    <div><input type="text" placeholder="Option B" name="questions[${i - 1}].option2"></div>
                    <div><input type="text" placeholder="Option C" name="questions[${i - 1}].option3"></div>
                    <div><input type="text" placeholder="Option D" name="questions[${i - 1}].option4"></div>
                </div><div class="input-group"><label>Correct Answer</label><select name="questions[${i - 1}].answer">
                    <option value="A">Option A</option>
                    <option value="B">Option B</option>
                    <option value="C">Option C</option>
                    <option value="D">Option D</option>
                </select></div>`;
            } else if (type === 'TRUE_FALSE') {
                innerHTML += `<div class="input-group"><label>Correct Answer</label><select name="questions[${i - 1}].answer">
                    <option value="TRUE">True</option>
                    <option value="FALSE">False</option>
                </select></div>`;
            } else {
                innerHTML += `<div class="input-group"><label>Expected Answer</label><textarea rows="2" placeholder="Key points..." name="questions[${i - 1}].answer"></textarea></div>`;
            }
            wrapper.innerHTML = innerHTML;
            container.appendChild(wrapper);
        }
    }

    // Data Gathering Helper
    function getFormData() {
        const questionsArr = [];
        const questionBlocks = document.querySelectorAll('.question-block');

        questionBlocks.forEach((block, idx) => {
            const q = {
                question: block.querySelector('.q-input')?.value || "Empty Question",
                answer: block.querySelector('[name*=".answer"]')?.value || "No Answer",
                option1: block.querySelector('[name*=".option1"]')?.value || null,
                option2: block.querySelector('[name*=".option2"]')?.value || null,
                option3: block.querySelector('[name*=".option3"]')?.value || null,
                option4: block.querySelector('[name*=".option4"]')?.value || null
            };
            questionsArr.push(q);
        });

        return {
            organisation: document.querySelector('[name="organisation"]').value || "Unknown Org",
            className: document.querySelector('[name="className"]').value || "General",
            subjectName: document.querySelector('[name="subjectName"]').value || "General",
            chapterName: document.querySelector('[name="chapterName"]').value || "General",
            topicName: document.querySelector('[name="topicName"]').value || "General",
            numberOfQuestions: parseInt(qCountSlider.value) || 0,
            questionMarks: parseInt(marksSlider.value) || 0,
            difficultyType: document.getElementById('difficultyInput').value || "MEDIUM",
            questionType: document.getElementById('typeInput').value || "MCQ",
            inputType: document.getElementById('sourceInput').value || "MANUAL",
            examType: document.getElementById('examType')?.value || "",
            totalTime: document.getElementById('totalTime')?.value || "",
            questions: questionsArr
        };
    }

    // Separate Button Logic
    const saveToBankBtn = document.getElementById('saveToBankBtn');
    const downloadPdfBtn = document.getElementById('downloadPdfBtn');

    if (saveToBankBtn) {
        saveToBankBtn.addEventListener('click', async () => {
            const originalText = saveToBankBtn.innerHTML;
            saveToBankBtn.disabled = true;
            saveToBankBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

            const data = getFormData();
            const orgName = data.organisation;

            try {
                // First: Upload logo if a new one was selected
                if (selectedLogoFile) {
                    const uploadSuccess = await uploadOrganisationLogo(selectedLogoFile, orgName);
                    if (!uploadSuccess) return; // Stop if logo upload fails
                }

                // Second: Save Questions
                const response = await fetch('/questions/save', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                const msg = await response.text();
                if (response.ok) {
                    showToast(msg, 'success');
                    // Reset selected file after successful save
                    selectedLogoFile = null;
                } else {
                    showToast("Error: " + msg, 'error');
                }
            } catch (err) {
                showToast("Network Error: " + err.message, 'error');
            } finally {
                saveToBankBtn.disabled = false;
                saveToBankBtn.innerHTML = originalText;
            }
        });
    }

    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', async () => {
            const originalText = downloadPdfBtn.innerHTML;
            downloadPdfBtn.disabled = true;
            downloadPdfBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating...';

            const data = getFormData();
            try {
                const response = await fetch('/questions/generate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const blob = await response.blob();
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = 'QuestionPaper.pdf';
                    document.body.appendChild(a);
                    a.click();
                    setTimeout(() => {
                        document.body.removeChild(a);
                        window.URL.revokeObjectURL(url);
                    }, 100);
                    showToast("PDF Download Started!", 'success');
                } else {
                    const errText = await response.text();
                    showToast("Server Error: " + errText, 'error');
                }
            } catch (err) {
                showToast("Network Error: " + err.message, 'error');
            } finally {
                downloadPdfBtn.disabled = false;
                downloadPdfBtn.innerHTML = originalText;
            }
        });
    }
    const homeBtn = document.getElementById('homeBtn');
    if (homeBtn) {
        homeBtn.addEventListener('click', () => {
            window.location.href = '/';
        });
    }

    function resetApp() {
        // 1. Reset standard form fields
        document.getElementById('qgenForm').reset();

        // 2. Reset custom selection cards (Difficulty, Type, Source)
        document.querySelectorAll('.option-card, .source-card').forEach(card => {
            card.classList.remove('selected', 'active');
        });

        // 3. Restore default selections in UI
        const defaultDiff = document.querySelector('.difficulty-option[data-value="MEDIUM"]');
        if (defaultDiff) defaultDiff.classList.add('selected');
        document.getElementById('difficultyInput').value = 'MEDIUM';

        const defaultType = document.querySelector('.type-option[data-value="MCQ"]');
        if (defaultType) defaultType.classList.add('selected');
        document.getElementById('typeInput').value = 'MCQ';

        const defaultSource = document.querySelector('.source-card[data-method="manual"]');
        if (defaultSource) defaultSource.classList.add('active');
        document.getElementById('sourceInput').value = 'MANUAL';

        // 4. Reset Sliders & Labels
        qCountSlider.max = 50; // Reset max to default
        marksSlider.max = 20;  // Reset max to default
        qCountSlider.value = 10;
        marksSlider.value = 5;
        qCountVal.textContent = 10;
        marksVal.textContent = 5;

        // Update slider background fills manually
        const qValPercent = (10 - 1) / (50 - 1) * 100;
        qCountSlider.style.backgroundSize = `${qValPercent}% 100%`;
        const mValPercent = (5 - 1) / (20 - 1) * 100;
        marksSlider.style.backgroundSize = `${mValPercent}% 100%`;

        // 5. Clear dynamic method areas (Manual questions/AI results)
        const manualContainer = document.getElementById('method-manual');
        if (manualContainer) {
            manualContainer.innerHTML = '';
            manualContainer.classList.add('hidden');
        }

        // 6. Reset PDF Upload Area to original state
        const uploadArea = document.getElementById('uploadArea');
        if (uploadArea) {
            uploadArea.innerHTML = `
                <i class="fas fa-cloud-upload-alt" style="font-size:3rem; color:var(--text-muted); margin-bottom:1rem;"></i>
                <p>Drag & Drop specific PDF here or click to browse</p>
                <input type="file" id="pdfFileInput" accept=".pdf" style="display:none;">
            `;
            // Re-bind listeners because we replaced the HTML
            const newFileInput = document.getElementById('pdfFileInput');
            if (newFileInput) {
                uploadArea.onclick = () => newFileInput.click();
                newFileInput.onchange = (e) => {
                    if (e.target.files.length > 0) handlePdfUpload(e.target.files[0]);
                };
            }
        }

        // 7. Reset method display
        document.querySelectorAll('.method-content').forEach(el => el.classList.add('hidden'));
        document.getElementById('method-manual').classList.remove('hidden');

        // 8. Return to Step 1
        currentStep = 1;
        updateUI();

        // 9. Smooth scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    // PDF Upload Logic
    const uploadArea = document.getElementById('uploadArea');
    const pdfFileInput = document.getElementById('pdfFileInput');

    if (uploadArea && pdfFileInput) {
        uploadArea.addEventListener('click', () => pdfFileInput.click());

        pdfFileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) handlePdfUpload(e.target.files[0]);
        });

        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.style.borderColor = 'var(--primary-color)';
            uploadArea.style.background = 'rgba(99, 102, 241, 0.1)';
        });

        uploadArea.addEventListener('dragleave', () => {
            uploadArea.style.borderColor = 'var(--glass-border)';
            uploadArea.style.background = 'transparent';
        });

        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.style.borderColor = 'var(--glass-border)';
            uploadArea.style.background = 'transparent';
            if (e.dataTransfer.files.length > 0) handlePdfUpload(e.dataTransfer.files[0]);
        });
    }

    // AI Generation Logic
    const aiGenBtn = document.getElementById('aiGenBtn');
    if (aiGenBtn) {
        aiGenBtn.addEventListener('click', async () => {
            const data = getFormData();
            const originalContent = aiGenBtn.innerHTML;
            aiGenBtn.disabled = true;
            aiGenBtn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> Generating...`;

            try {
                const response = await fetch('/questions/ai-generate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const questions = await response.json();
                    populateExtractedQuestions(questions);
                    showToast(`AI successfully generated ${questions.length} questions!`, 'success');
                } else {
                    showToast("AI Generation failed. Check your API key or network.", 'error');
                }
            } catch (err) {
                showToast("AI Error: " + err.message, 'error');
            } finally {
                aiGenBtn.disabled = false;
                aiGenBtn.innerHTML = originalContent;
            }
        });
    }

    async function handlePdfUpload(file) {
        if (!file.type.includes('pdf')) {
            showToast("Please upload a PDF file.", 'info');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        uploadArea.innerHTML = `<div class="loader"></div><p style="margin-top:1rem;">Extracting questions...</p>`;

        try {
            const response = await fetch('/questions/import-pdf', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const questions = await response.json();
                populateExtractedQuestions(questions);
                showToast(`Successfully extracted ${questions.length} questions!`, 'success');
            } else {
                showToast("Failed to extract questions. The PDF might be too complex.", 'error');
            }
        } catch (err) {
            showToast("Upload Error: " + err.message, 'error');
        } finally {
            uploadArea.innerHTML = `<i class="fas fa-check-circle" style="font-size:3rem; color:#10b981; margin-bottom:1rem;"></i><p>File Uploaded: ${file.name}</p><p style="font-size:0.8rem; color:var(--text-muted)">Click to change file</p>`;
        }
    }

    function populateExtractedQuestions(questions) {
        const container = document.getElementById('method-manual');
        container.innerHTML = '';
        container.classList.remove('hidden');

        const header = document.createElement('div');
        header.innerHTML = `<h3 style="margin-bottom:1.5rem; color:var(--primary-color)">Extracted ${questions.length} Questions</h3>`;
        container.appendChild(header);

        questions.forEach((q, i) => {
            const wrapper = document.createElement('div');
            wrapper.className = "question-block";
            wrapper.style.cssText = "background:rgba(255,255,255,0.02); padding:1.5rem; border-radius:12px; margin-bottom:1.5rem; border:1px solid var(--glass-border);";

            let innerHTML = `<div class="input-group"><label style="color:var(--secondary-color)">Question ${i + 1}</label><textarea rows="2" class="q-input" name="questions[${i}].question">${q.question || ''}</textarea></div>`;

            const type = document.getElementById('typeInput').value;
            if (type === 'MCQ') {
                innerHTML += `<div style="display:grid; grid-template-columns: 1fr 1fr; gap:1rem; margin-bottom:1rem;">
                    <div><input type="text" placeholder="Option A" name="questions[${i}].option1" value="${q.option1 || ''}"></div>
                    <div><input type="text" placeholder="Option B" name="questions[${i}].option2" value="${q.option2 || ''}"></div>
                    <div><input type="text" placeholder="Option C" name="questions[${i}].option3" value="${q.option3 || ''}"></div>
                    <div><input type="text" placeholder="Option D" name="questions[${i}].option4" value="${q.option4 || ''}"></div>
                </div>`;
            }

            innerHTML += `<div class="input-group"><label>Correct/Expected Answer</label><textarea rows="1" placeholder="Answer..." name="questions[${i}].answer">${q.answer || ''}</textarea></div>`;

            wrapper.innerHTML = innerHTML;
            container.appendChild(wrapper);
        });

        // Update slider to match extracted count
        if (questions.length > qCountSlider.max) {
            qCountSlider.max = questions.length;
        }
        qCountSlider.value = questions.length;
        qCountVal.textContent = questions.length;
        const value = (qCountSlider.value - qCountSlider.min) / (qCountSlider.max - qCountSlider.min) * 100;
        qCountSlider.style.backgroundSize = `${value}% 100%`;

        // Refresh summary if it exists
        updateReviewSummary();
        updateFinalStats();
    }

    function updateUI() {
        document.querySelectorAll('.form-step').forEach(step => step.classList.remove('active'));
        const activeStep = document.querySelector(`.form-step[data-step="${currentStep}"]`);
        if (activeStep) activeStep.classList.add('active');

        document.querySelectorAll('.step-indicator').forEach((ind, idx) => {
            if (idx + 1 === currentStep) { ind.classList.add('active'); ind.classList.remove('completed'); ind.innerHTML = idx + 1; }
            else if (idx + 1 < currentStep) { ind.classList.add('completed'); ind.classList.remove('active'); ind.innerHTML = '<i class="fas fa-check"></i>'; }
            else { ind.classList.remove('active', 'completed'); ind.innerHTML = idx + 1; }
        });

        if (currentStep === 4) updateReviewSummary();
        if (currentStep === 5) updateFinalStats();

        prevBtn.style.display = currentStep === 1 ? 'none' : 'block';

        if (currentStep === totalSteps) {
            nextBtn.style.display = 'none';
            finalSubmitBtn.style.display = 'none'; // Hide footer button in Step 5
        } else {
            nextBtn.style.display = 'block';
            finalSubmitBtn.style.display = 'none';
            nextBtn.innerHTML = currentStep === 4 ? 'Reviewed & Want to Save' : 'Next Step';
        }
    }

    function updateReviewSummary() {
        const summary = document.getElementById('review-summary');
        const org = document.querySelector('[name="organisation"]').value || "General";
        const subject = document.querySelector('[name="subjectName"]').value || "Subject";
        const topic = document.querySelector('[name="topicName"]').value || "Topic";
        const count = qCountSlider.value;
        const marks = marksSlider.value;
        summary.innerHTML = `<div style="display:grid; grid-template-columns: 1fr 1fr; gap:1rem; background:rgba(255,255,255,0.03); padding:1rem; border-radius:10px;">
            <div><span style="color:var(--text-muted)">Organisation:</span><br><strong>${org}</strong></div>
            <div><span style="color:var(--text-muted)">Subject:</span><br><strong>${subject}</strong></div>
            <div><span style="color:var(--text-muted)">Topic:</span><br><strong>${topic}</strong></div>
            <div><span style="color:var(--text-muted)">Questions:</span><br><strong>${count} (x ${marks} Marks)</strong></div>
        </div>`;
    }

    function updateFinalStats() {
        const stats = document.getElementById('final-stats');
        const count = qCountSlider.value;
        const marks = marksSlider.value;
        stats.innerHTML = `<div style="display:flex; justify-content:space-around; align-items:center;">
            <div><div style="font-size:2rem; font-weight:800; color:var(--primary-color)">${count}</div><div style="font-size:0.8rem; color:var(--text-muted)">Questions</div></div>
            <div style="width:1px; height:40px; background:var(--glass-border);"></div>
            <div><div style="font-size:2rem; font-weight:800; color:var(--secondary-color)">${count * marks}</div><div style="font-size:0.8rem; color:var(--text-muted)">Total Marks</div></div>
        </div>`;
    }

    // Logo Upload Logic
    const logoPreviewCard = document.getElementById('logoPreviewCard');
    const logoInput = document.getElementById('logoInput');
    const logoPreview = document.getElementById('logoPreview');
    const placeholderIcon = document.getElementById('placeholderIcon');
    const orgInput = document.querySelector('[name="organisation"]');
    let lastUploadedOrg = "";
    let selectedLogoFile = null; // Store logo file in memory

    if (logoPreviewCard && logoInput) {
        logoPreviewCard.addEventListener('click', () => logoInput.click());

        logoInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const orgName = orgInput.value.trim();
                if (!orgName) {
                    showToast("Please enter an Organisation name first", "info");
                    logoInput.value = "";
                    return;
                }

                const reader = new FileReader();
                reader.onload = (event) => {
                    logoPreview.src = event.target.result;
                    logoPreview.style.display = 'block';
                    placeholderIcon.style.display = 'none';
                    selectedLogoFile = file; // Store file for final save
                    lastUploadedOrg = orgName;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    async function uploadOrganisationLogo(file, orgName) {
        const formData = new FormData();
        formData.append('organisation', orgName);
        formData.append('logo', file);

        try {
            const response = await fetch('/api/organisations/upload-logo', {
                method: 'POST',
                body: formData
            });
            if (response.ok) {
                console.log("Logo saved for " + orgName);
                return true;
            } else {
                showToast("Failed to save logo", "error");
                return false;
            }
        } catch (err) {
            console.error("Logo upload failed", err);
            showToast("Logo Upload Error: " + err.message, "error");
            return false;
        }
    }

    // Auto-fetch logo when org name is typed
    let orgTimeout;
    if (orgInput) {
        orgInput.addEventListener('input', () => {
            const name = orgInput.value.trim();

            // If user cleared the name, hide preview
            if (!name) {
                logoPreview.style.display = 'none';
                placeholderIcon.style.display = 'block';
                lastUploadedOrg = "";
                return;
            }

            // If we just uploaded for this name, don't trigger fetch
            if (name === lastUploadedOrg) return;

            clearTimeout(orgTimeout);
            orgTimeout = setTimeout(async () => {
                const updatedName = orgInput.value.trim();
                if (updatedName && updatedName !== lastUploadedOrg) {
                    const resp = await fetch(`/api/organisations/logo/${updatedName}`);
                    if (resp.ok) {
                        const blob = await resp.blob();
                        logoPreview.src = URL.createObjectURL(blob);
                        logoPreview.style.display = 'block';
                        placeholderIcon.style.display = 'none';
                    } else {
                        // Only hide if we aren't showing a local upload for this name
                        if (updatedName !== lastUploadedOrg) {
                            logoPreview.style.display = 'none';
                            placeholderIcon.style.display = 'block';
                        }
                    }
                }
            }, 800);
        });
    }

    // Mobile Sidebar Toggle
    const menuToggle = document.getElementById('menuToggle');
    const closeSidebar = document.getElementById('closeSidebar');
    const sidebar = document.querySelector('.sidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');

    if (sidebar && sidebarOverlay) {
        const toggleSidebar = () => {
            sidebar.classList.toggle('active');
            sidebarOverlay.classList.toggle('active');
        };

        if (menuToggle) menuToggle.addEventListener('click', toggleSidebar);
        if (closeSidebar) closeSidebar.addEventListener('click', toggleSidebar);
        sidebarOverlay.addEventListener('click', toggleSidebar);
    }

    // === Global Search Bar Logic ===
    function setupGlobalSearch() {
        const searchInput = document.querySelector('.search-input');
        if (!searchInput) return;

        const container = searchInput.parentElement;
        const menu = document.createElement('div');
        menu.className = 'custom-dropdown-menu';
        menu.style.width = '100.5%';
        menu.style.left = '-1px';
        menu.style.top = 'calc(100% + 10px)';
        container.style.position = 'relative';
        container.appendChild(menu);

        let searchTimeout;
        searchInput.addEventListener('input', () => {
            clearTimeout(searchTimeout);
            const keyword = searchInput.value.trim();
            if (keyword.length < 2) {
                menu.style.display = 'none';
                return;
            }

            searchTimeout = setTimeout(async () => {
                try {
                    const response = await fetch(`/questions/search?keyword=${encodeURIComponent(keyword)}`);
                    if (response.ok) {
                        const questions = await response.json();
                        if (questions.length === 0) {
                            menu.innerHTML = '<div class="custom-dropdown-item"><span>No matching questions in bank</span></div>';
                        } else {
                            menu.innerHTML = questions.map(q => `
                                <div class="custom-dropdown-item" style="flex-direction: column; align-items: flex-start; gap: 4px; padding: 12px 20px; border-bottom: 1px solid var(--glass-border);">
                                    <div style="font-weight: 600; font-size: 0.95rem; color: var(--primary-color); line-height: 1.4;">${q.question}</div>
                                    <div style="font-size: 0.75rem; color: var(--text-muted); display: flex; gap: 12px; align-items: center; margin-top: 4px;">
                                        <span style="display: flex; align-items: center; gap: 4px;"><i class="fas fa-book" style="font-size: 0.7rem;"></i> ${q.subjectName}</span>
                                        <span style="display: flex; align-items: center; gap: 4px;"><i class="fas fa-layer-group" style="font-size: 0.7rem;"></i> ${q.topicName}</span>
                                        <span style="background: rgba(79, 70, 229, 0.1); color: var(--primary-color); padding: 2px 8px; border-radius: 4px; font-weight: 600; font-size: 0.65rem; text-transform: uppercase;">${q.difficultyType}</span>
                                    </div>
                                </div>
                            `).join('');
                        }
                        menu.style.display = 'block';
                    }
                } catch (err) { console.error('Search error', err); }
            }, 300);
        });

        document.addEventListener('mousedown', (e) => {
            if (!container.contains(e.target)) menu.style.display = 'none';
        });

        searchInput.addEventListener('focus', () => {
            if (searchInput.value.trim().length >= 2) menu.style.display = 'block';
        });
    }

    setupGlobalSearch();
    updateUI();
});
