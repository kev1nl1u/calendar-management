// readapted from https://codepen.io/alvarotrigo/pen/bGLpROa



// Calendar controller class
class CalendarControl {
	constructor(is_editing = false) {
		// Initialize calendar and localDate variables
		this.view = new Date();
		this.selected = new Date();
		this.localDate = new Date();
		
		this.prevMonthLastDate = null;
		this.calWeekDays = ["Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab"];
		this.calMonthName = [
			"Gen",
			"Feb",
			"Mar",
			"Apr",
			"Mag",
			"Giu",
			"Lug",
			"Ago",
			"Set",
			"Ott",
			"Nov",
			"Dic"
		];

		this.is_editing = is_editing; // if the user is editing, show the delete button

		// Call the init method to initialize the calendar control
		this.init();
	}

	/*** USEFUL METHODS ***/

	// Get the number of days in a given month and year
	daysInMonth(month, year) {
		return new Date(year, month, 0).getDate();
	}

	// Get the first day of the current month
	firstDay() {
		return new Date(this.view.getFullYear(), this.view.getMonth(), 1);
	}

	// Get the last day of the current month
	lastDay() {
		return new Date(this.view.getFullYear(), this.view.getMonth() + 1, 0);
	}

	// Get the number of the first day of the current month (1-7)
	firstDayNumber() {
		return this.firstDay().getDay() + 1;
	}

	// Get the number of the last day of the current month (1-7)
	lastDayNumber() {
		return this.lastDay().getDay() + 1;
	}

	// Get the last date of the previous month
	getPreviousMonthLastDate() {
		return new Date(
			this.view.getFullYear(),
			this.view.getMonth(),
			0
		).getDate();
	}

	/***/

	/*** NAVIGATION METHODS ***/

	// Navigate to the previous month
	navigateToPreviousMonth() {
		this.view.setMonth(this.view.getMonth() - 1);
		this.printCalendarDates();
	}

	// Navigate to the next month
	navigateToNextMonth() {
		this.view.setMonth(this.view.getMonth() + 1);
		this.printCalendarDates();
	}

	// Navigate to the current month
	navigateToCurrentMonth() {
		const currentMonth = this.localDate.getMonth();
		const currentYear = this.localDate.getFullYear();
		this.view.setMonth(currentMonth);
		this.view.setYear(currentYear);
		this.selected = new Date();
		this.printCalendarDates();
		document.getElementsByClassName('events-header-date')[0].innerHTML = this.selected.getDate() + ' ' + this.calMonthName[this.selected.getMonth()] + ' ' + this.selected.getFullYear();
		this.loadEvents();
	}

	navigateToPreviousDay() {
		this.selected.setDate(this.selected.getDate() - 1);
		// if month changes, change the month
		if (this.selected.getMonth() !== this.view.getMonth()) {
			this.view.setMonth(this.selected.getMonth());
		}
		this.printCalendarDates();
		document.getElementsByClassName('events-header-date')[0].innerHTML = this.selected.getDate() + ' ' + this.calMonthName[this.selected.getMonth()] + ' ' + this.selected.getFullYear();
		this.loadEvents();
	}

	navigateToNextDay() {
		this.selected.setDate(this.selected.getDate() + 1);
		// if month changes, change the month
		if (this.selected.getMonth() !== this.view.getMonth()) {
			this.view.setMonth(this.selected.getMonth());
		}
		this.printCalendarDates();
		document.getElementsByClassName('events-header-date')[0].innerHTML = this.selected.getDate() + ' ' + this.calMonthName[this.selected.getMonth()] + ' ' + this.selected.getFullYear();
		this.loadEvents();
	}

	// Select a date in the calendar
	selectDate(e) {
		console.log(
			`${e.target.textContent} ${
				this.calMonthName[this.selected.getMonth()]
			} ${this.selected.getFullYear()}`
		);

		// Set active class for the selected date
		document.querySelectorAll('.number-item').forEach(function(item) {
			item.classList.remove('calendar-active');
		});
		e.target.parentElement.classList.add('calendar-active');

		// update calendar
		this.selected.setDate(e.target.textContent);
		this.selected.setMonth(this.view.getMonth());

		document.getElementsByClassName('events-header-date')[0].innerHTML = e.target.textContent + ' ' + this.calMonthName[this.selected.getMonth()] + ' ' + this.selected.getFullYear();

		this.loadEvents();
	}

	/***/

	/*** PRINT METHODS ***/

	// print calendar header
	printCalendarHeader() {
		document.querySelector(
			".calendar"
		).innerHTML += `<div class="calendar-inner"><div class="calendar-controls">
				<div class="calendar-prev"><a><svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128"><path fill="#666" d="M88.2 3.8L35.8 56.23 28 64l7.8 7.78 52.4 52.4 9.78-7.76L45.58 64l52.4-52.4z"/></svg></a></div>
				<div class="calendar-year-month">
				<div class="calendar-month-label"></div>
				<div class="calendar-year-label"></div>
				</div>
				<div class="calendar-next"><a><svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128"><path fill="#666" d="M38.8 124.2l52.4-52.42L99 64l-7.77-7.78-52.4-52.4-9.8 7.77L81.44 64 29 116.42z"/></svg></a></div>
				</div>
				<div class="calendar-today-date">Oggi: 
					${this.calWeekDays[this.localDate.getDay()]}, 
					${this.localDate.getDate()}, 
					${this.calMonthName[this.localDate.getMonth()]} 
					${this.localDate.getFullYear()}
				</div>
				<div class="calendar-body"></div></div>`;
	}

	// print calendar dates
	printCalendarDates() {
		document.querySelector(".calendar .calendar-body").innerHTML = "";
		
		// print days of the week
		for (let i = 0; i < this.calWeekDays.length; i++) {
			document.querySelector(
				".calendar .calendar-body"
			).innerHTML += `<div>${this.calWeekDays[i]}</div>`;
		}

		// print month and year
		const monthLabel = document.querySelector(
			".calendar .calendar-month-label"
		);
		monthLabel.innerHTML = this.calMonthName[this.view.getMonth()];
		const yearLabel = document.querySelector(".calendar .calendar-year-label");
		yearLabel.innerHTML = this.view.getFullYear();

		let count = 1;
		let prevDateCount = 0;

		this.prevMonthLastDate = this.getPreviousMonthLastDate();
		let prevMonthDatesArray = [];
		let calendarDays = this.daysInMonth(
			this.view.getMonth() + 1,
			this.view.getFullYear()
		);

		// Dates of current month
		for (let i = 1; i < calendarDays; i++) {
			if (i < this.firstDayNumber()) {
				prevDateCount += 1;
				document.querySelector(
					".calendar .calendar-body"
				).innerHTML += `<div class="prev-dates"></div>`;
				prevMonthDatesArray.push(this.prevMonthLastDate--);
			} else {
				document.querySelector(
					".calendar .calendar-body"
				).innerHTML += `<div class="number-item" data-num=${count}><a class="dateNumber" href="#">${count++}</a></div>`;
			}
		}

		// Remaining dates after month dates
		for (let j = 0; j < prevDateCount + 1; j++) {
			document.querySelector(
				".calendar .calendar-body"
			).innerHTML += `<div class="number-item" data-num=${count}><a class="dateNumber" href="#">${count++}</a></div>`;
		}

		this.highlightSelected();
		this.getPrevMonthDates(prevMonthDatesArray);
		this.getNextMonthDates();

		const dateNumber = document.querySelectorAll(".calendar .dateNumber");

		for (let i = 0; i < dateNumber.length; i++) {
			dateNumber[i].addEventListener(
				"click",
				this.selectDate.bind(this),
				false
			);
		}
	}

	// Highlight date
	highlightSelected() {
		if (this.selected.getMonth() === this.view.getMonth()) { // check if the selected date is in the current month, if not then user is exploring other months
			document
				.querySelectorAll(".number-item")
				[this.selected.getDate() - 1].classList.add("calendar-active");
		}
		// highlight today's date if it is the view month
		if(this.localDate.getMonth() === this.view.getMonth() && this.localDate.getFullYear() === this.view.getFullYear()){
			document.querySelectorAll(".number-item")[this.localDate.getDate() - 1].classList.add("calendar-today");
		}
	}

	// get the previous month dates in the calendar
	getPrevMonthDates(dates) {
		dates.reverse();
		for (let i = 0; i < dates.length; i++) {
			if (document.querySelectorAll(".prev-dates")) {
				document.querySelectorAll(".prev-dates")[i].textContent = dates[i];
			}
		}
	}

	// get the next month dates in the calendar
	getNextMonthDates() {
		const childElemCount = document.querySelector('.calendar-body').childElementCount;

		// 7 lines
		if (childElemCount > 42) {
			const diff = 49 - childElemCount;
			this.loopThroughNextDays(diff);
		}

		// 6 lines
		if (childElemCount > 35 && childElemCount <= 42) {
			const diff = 42 - childElemCount;
			this.loopThroughNextDays(diff);
		}
	}

	// Loop through and get the next month dates
	loopThroughNextDays(count) {
		if (count > 0) {
			for (let i = 1; i <= count; i++) {
				document.querySelector('.calendar-body').innerHTML += `<div class="next-dates">${i}</div>`;
			}
		}
	}

	// Load events for the selected date
	loadEvents() {
		// POST REQ TO GET EVENTS OF SELECTED DATE
		fetch('/post/getEvents', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({date: this.selected.toISOString().split('T')[0]})
		})
		.then(response => response.json()) // parse the JSON from the server
		.then(data => {
			data = data.events;
			console.log(data);

			// readapted from sorting of CercaEventi (ferminotify.me)
			// if end exists, sort by start and end
			if (data.length > 0 && data[0].end) {
				data.sort(function(a, b) {
					data.sort(function(a, b) {
						if (a.start[0].date && b.start[0].date) { // if both events have a start date
							if (moment(a.start[0].date + "T" + a.start[0].time).isSame(moment(b.start[0].date + "T" + b.start[0].time))) { // if both events have the same start date and time
								return moment(a.end[0].date + "T" + a.end[0].time).diff(moment(b.end[0].date + "T" + b.end[0].time));
							}
						} else { // if both events don't have a start date
							if (moment(a.start[0].date).isSame(moment(b.start[0].date))) { // if both events have the same start date
								return moment(a.end[0].date).diff(moment(b.end[0].date));
							}
						}
					});
				});
			}

			const events_list = document.getElementById('events-list');
			events_list.innerHTML = '';
			var html = '';

			// for each event, print the event
			data.forEach(event => {
				html += '<div class="event">';

				if(this.is_editing) html += '<a class="btn text" onclick="showModal(\'delete\', ' + event.id + ')"><span class="material-symbols-outlined">delete</span></a>';

				html += '<div class="event-info">';

				// if there is array classi in event
				if(event.classi){
					html += '<div class="flex">';
					event.classi.forEach(classe => {
						html += `<p class="event-info-classe tag">${classe}</p>`;
					});
					html += '</div>';
				}
				html += `<p>${event.summary}</p>`;
				if(event.location){
					html += `<p>${event.location}</p>`;
				}
				if(event.insegnanti){
					event.insegnanti.forEach(insegnante => {
						html += `<p>${insegnante}</p>`;
					});
				}

				if(event.end){
					html += `</div>
						<div class="event-time">
							<p class="event-time-start">
								<span class="start-end-text"><span class="material-symbols-outlined">start</span></span>
								<span>${event.start[0].time.split(":").slice(0, 2).join(":")}</span>
							</p>
							<p class="event-time-end">
								<span class="start-end-text"><span class="material-symbols-outlined mirrorElement">start</span></span>
								<span>${event.end[0].time.split(":").slice(0, 2).join(":")}</span>
							</p>
						</div>
					</div>`;
				}else{
					html += `</div>
						<div class="event-time">
							<p class="event-time-start">
								<span class="start-end-text"><span class="material-symbols-outlined">schedule</span></span>
								<span>${event.start[0].time.split(":").slice(0, 2).join(":")}</span>
							</p>
						</div>
					</div>`;
				}
			});
			events_list.innerHTML += html;
		})
		.catch(error => {
			console.error(error);
		});
	}

	// Initialize
	init() {
		this.printCalendarHeader();
		this.printCalendarDates();

		const prevBtn = document.querySelector(".calendar .calendar-prev a");
		const nextBtn = document.querySelector(".calendar .calendar-next a");
		const todayDate = document.querySelector(".calendar .calendar-today-date");
		
		// Attach event listeners to the previous, next and today buttons
		prevBtn.addEventListener(
			"click",
			this.navigateToPreviousMonth.bind(this)
		);
		nextBtn.addEventListener(
			"click",
			this.navigateToNextMonth.bind(this)
		);
		todayDate.addEventListener(
			"click",
			this.navigateToCurrentMonth.bind(this)
		);

		document.getElementsByClassName('events-header-date')[0].innerHTML = this.selected.getDate() + ' ' + this.calMonthName[this.selected.getMonth()] + ' ' + this.selected.getFullYear();

		this.loadEvents();
	}
}

// calendarControl is initialized in the html file

function prevDay(){
	calendarControl.navigateToPreviousDay();
}

function nextDay() {
	calendarControl.navigateToNextDay();
}

// arrow left and right to navigate through days
document.addEventListener('keydown', function(event) {
	if(event.key === 'ArrowLeft'){
		calendarControl.navigateToPreviousDay();
	}
	if(event.key === 'ArrowRight'){
		calendarControl.navigateToNextDay();
	}
});