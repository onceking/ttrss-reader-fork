/*
 * Copyright (c) 2015, Nils Braden
 *
 * This file is part of ttrss-reader-fork. This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; If
 * not, see http://www.gnu.org/licenses/.
 */

package org.ttrssreader.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.ttrssreader.R;
import org.ttrssreader.controllers.DBHelper;
import org.ttrssreader.model.pojos.Article;
import org.ttrssreader.model.pojos.Feed;
import org.ttrssreader.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FeedHeadlineAdapter extends MainAdapter {

	//	private static final String TAG = FeedHeadlineAdapter.class.getSimpleName();

	private final int feedId;
	private final boolean selectArticlesForCategory;

	private final static List<Pair<String, Integer>> TIME_UNITS = Arrays.asList(
			Pair.create("m", 60),
			Pair.create("h", 3600),
			Pair.create("d", 86400),
			Pair.create("w", 7*86400),
			Pair.create("M", 30*86400),
			Pair.create("y", 365*86400)
	);

	public FeedHeadlineAdapter(Context context, int feedId, boolean selectArticlesForCategory) {
		super(context);
		this.feedId = feedId;
		this.selectArticlesForCategory = selectArticlesForCategory;
	}

	@Override
	public Object getItem(int position) {
		Article ret = new Article();
		Cursor cur = getCursor();
		if (cur == null)
			return ret;

		if (cur.getCount() >= position) {
			if (cur.moveToPosition(position)) {
				return getArticle(cur);
			}
		}
		return ret;
	}

	private static void setImage(ImageView icon, Article a) {
		if (a.isUnread) {
			icon.setBackgroundResource(R.drawable.articleunread48);
		} else {
			icon.setBackgroundResource(R.drawable.articleread48);
		}

		if (a.isStarred && a.isPublished) {
			icon.setImageResource(R.drawable.published_and_starred48);
		} else if (a.isStarred) {
			icon.setImageResource(R.drawable.star_yellow48);
		} else if (a.isPublished) {
			icon.setImageResource(R.drawable.published_blue48);
		} else {
			icon.setBackground(null);
			if (a.isUnread) {
				icon.setImageResource(R.drawable.articleunread48);
			} else {
				icon.setImageResource(R.drawable.articleread48);
			}
		}
	}

	private static void setFeedImage(ImageView icon, Feed f) {
		if (f.icon != null) {
			icon.setVisibility(View.VISIBLE);
			icon.setImageBitmap(BitmapFactory.decodeByteArray(f.icon, 0, f.icon.length));
		} else {
			icon.setVisibility(View.GONE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return View.inflate(context, R.layout.item_feedheadline, null);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			holder = new ViewHolder();
//			holder.icon = (ImageView) view.findViewById(R.id.icon);
			holder.feedicon = (ImageView) view.findViewById(R.id.feedicon);
			holder.title = (TextView) view.findViewById(R.id.title);
//			holder.updateDate = (TextView) view.findViewById(R.id.updateDate);
//			holder.dataSource = (TextView) view.findViewById(R.id.dataSource);
			view.setTag(holder);
		}

		final Article a = getArticle(cursor);
		final Feed f = DBHelper.getInstance().getFeed(a.feedId);

//		setImage(holder.icon, a);
		setFeedImage(holder.feedicon, f);

		String title = a.isUnread ? "◯ " :"⬤ ";

		final double age = Math.abs((new Date()).getTime() - a.updated.getTime()) / 1000.0;
		for ( Pair<String, Integer> time_unit : TIME_UNITS) {
			long age_scaled = Math.round(age / time_unit.second);
			if (age_scaled < 10) {
				title += String.format("[%d%s] ", age_scaled, time_unit.first);
				break;
			}
		}
		title += a.title;

		holder.title.setText(title);
		holder.title.setAlpha(a.isUnread ? 1 : 0.7f);
	}

	private static Article getArticle(Cursor cur) {
		Article ret = new Article();
		ret.id = cur.getInt(0);
		ret.feedId = cur.getInt(1);
		ret.title = cur.getString(2);
		ret.isUnread = cur.getInt(3) != 0;
		ret.updated = new Date(cur.getLong(4));
		ret.isStarred = cur.getInt(5) != 0;
		ret.isPublished = cur.getInt(6) != 0;
		ret.note = cur.getString(7);
		ret.feedTitle = cur.getString(8);
		return ret;
	}

	private static class ViewHolder {
		TextView title;
		ImageView feedicon;
	}

}
