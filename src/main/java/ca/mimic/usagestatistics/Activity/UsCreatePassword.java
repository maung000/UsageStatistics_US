package ca.mimic.usagestatistics.Activity;

/*
 * Copyright © 2014 Jeff Corcoran
 *
 * This file is part of Hangar.
 *
 * Hangar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the permission, or
 * (at your option) any later version.
 *
 * Hangar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hangar.  If not, see <http://www.gnu.org/us_permissions/>.
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import ca.mimic.usagestatistics.R;

public class UsCreatePassword {
    Context context;
    View mUsCreatePassword;

    public UsCreatePassword(Context context) {
        this.context = context;
    }

    @SuppressLint("InflateParams")
    public View getView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUsCreatePassword = inflater.inflate(R.layout.us_create_password, null);

        return mUsCreatePassword;
    }
}

