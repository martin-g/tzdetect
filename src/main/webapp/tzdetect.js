var MINUTE_STEP = 15;

function getShift(d, comp) {
	//we assume here jan1 is not on the "DST edge"
	var present = false;
	var y = new Date().getFullYear();
	d = !!d ? d : new Date(y, 0, 1, 0, 0, 0, 0);
	var offset = -d.getTimezoneOffset(), curOffset, M = d.getMonth(), D = d.getDate(), h = d.getHours(), m = d.getMinutes();
	while (y == d.getFullYear()) {
		d.setMonth(++M);
		curOffset = -d.getTimezoneOffset();
		if (!comp(curOffset, offset)) {
			offset = curOffset; //Southern Hemisphere DST end is earlier than DST start
			continue;
		}
		if (comp(curOffset, offset)) {
			present = true;
			d.setMonth(--M);
			break;
		}
	}
	if (present) {
		do {
			d.setDate(++D);
			curOffset = -d.getTimezoneOffset();
		} while (curOffset == offset);
		d.setMonth(M, --D);
		do {
			d.setHours(++h);
			curOffset = -d.getTimezoneOffset();
		} while (curOffset == offset && h == d.getHours()); //DST hole
		d.setHours(--h);
		do {
			d.setHours(h, m += MINUTE_STEP);
			curOffset = -d.getTimezoneOffset();
		} while (curOffset == offset);
		m -= 60; //jump back DST hole
	}
	if (m == 60) {
		m = 0;
		h++;
	}
	if (h == 24) {
		h = 0;
		D++;
	}
	return {present: present, offset: offset, shift: {year: y, month: M, day: D, hour: h, minute: m}};
}

function startComp(c1, c2) {
	return c1 > c2;
}

function endComp(c1, c2) {
	return c1 < c2;
}

function getRules() {
	var s1 = getShift(null, startComp), s2, r = {};
	if (s1.present) {
		r.present = true;
		r.offset = s1.offset;
		r.start = [];
		r.start.push(s1.shift);
		s2 = getShift(new Date(new Date().getFullYear(), s1.shift.month + 1, 1, 0, 0, 0, 0), startComp);
		if (s2.present) {
			r.start.push(s2.shift);
		}
		s1 = getShift(null, endComp);
		r.end = [];
		r.end.push(s1.shift);
		s2 = getShift(new Date(new Date().getFullYear(), s1.shift.month + 1, 1, 0, 0, 0, 0), endComp);
		if (s2.present) {
			r.end.push(s2.shift);
		}
	} else {
		r.present = false;
		r.offset =  -(new Date()).getTimezoneOffset();
	}
	return JSON.stringify(r);
}
