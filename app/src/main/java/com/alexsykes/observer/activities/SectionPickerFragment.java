package com.alexsykes.observer.activities;

import androidx.fragment.app.Fragment;

import com.alexsykes.observer.R;

    public class SectionPickerFragment extends Fragment {

        public SectionPickerFragment() {
            super((R.layout.fragment_section_picker));
        }

        public static int increment(int section, int numsections) {
            section++;
            if (section > numsections) {
                section = 1;
            }
            return section;
        }

        public static int decrement(int section, int numSections) {
            section--;
            if (section == 0){
                section = numSections;
            }
            return section;
        }
    }
